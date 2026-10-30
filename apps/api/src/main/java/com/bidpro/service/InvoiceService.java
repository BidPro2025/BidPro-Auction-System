package com.bidpro.service;

import com.bidpro.dao.BidDao;
import com.bidpro.payment.PaymentRecordRepository;
import com.bidpro.dao.InvoiceDao;
import com.bidpro.dao.ProductDao;
import com.bidpro.dao.UserDao;
import com.bidpro.dto.InvoiceDTO;
import com.bidpro.entities.Bid;
import com.bidpro.entities.Invoice;
import com.bidpro.entities.Product;
import com.bidpro.entities.User;
import jakarta.persistence.EntityNotFoundException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class InvoiceService {
    private final InvoiceDao invoices;
    private final ProductDao products;
    private final BidDao bids;
    private final UserDao users;
    private final CurrentUserService current;
    private final PaymentRecordRepository paymentOrders;
    private final NotificationService notifications;
    private final LogService audit;

    public InvoiceService(InvoiceDao invoices, ProductDao products, BidDao bids, UserDao users, CurrentUserService current, PaymentRecordRepository paymentOrders, NotificationService notifications, LogService audit) {
        this.invoices = invoices;
        this.products = products;
        this.bids = bids;
        this.users = users;
        this.current = current;
        this.paymentOrders = paymentOrders;
        this.notifications = notifications;
        this.audit = audit;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public InvoiceDTO createInvoice(String productID) {
        current.requireApproved();
        // The same lock serializes bids, closing and settlement, including
        // repeat settlement requests arriving concurrently.
        Product product = products.findForUpdateByProductID(productID)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
        var existing = invoices.findByProductID(productID);
        if (existing.isPresent()) {
            requireParty(existing.get());
            return toDTO(existing.get());
        }
        if (product.getAuctionEndDate() == null || LocalDateTime.now().isBefore(product.getAuctionEndDate()))
            throw new IllegalArgumentException("The auction must close before an invoice can be generated");
        if (product.getProductStatus() != Product.ProductStatus.APPROVED)
            throw new IllegalArgumentException("Only an approved auction can be settled");
        Bid winning = bids.findFirstByProductIDOrderByAmountDescTimestampAsc(productID)
                .orElseThrow(() -> new IllegalArgumentException("There are no bids to settle"));
        String requester = current.id();
        if (!current.isAdmin() && !requester.equals(product.getSellerID()) && !requester.equals(winning.getBidderID()))
            throw new AccessDeniedException("Only the seller or winning bidder can generate this invoice");
        if (product.getAuctionID() == null) throw new IllegalArgumentException("The product has no linked auction");
        return persistInvoice(product, winning, current.id());
    }

    /** Internal scheduler entry point; no controller exposes system settlement. */
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    public Optional<InvoiceDTO> settleExpiredAuction(String productID) {
        Product product = products.findForUpdateByProductID(productID).orElse(null);
        if (product == null || product.getAuctionID() == null || product.getAuctionEndDate() == null ||
                product.getAuctionEndDate().isAfter(LocalDateTime.now())) return Optional.empty();
        var existing = invoices.findByProductID(productID);
        if (existing.isPresent()) return Optional.of(toDTO(existing.get()));
        if (product.getProductStatus() != Product.ProductStatus.APPROVED) return Optional.empty();
        return bids.findFirstByProductIDOrderByAmountDescTimestampAsc(productID)
                .map(winning -> persistInvoice(product, winning, "SYSTEM_SETTLEMENT"));
    }

    private InvoiceDTO persistInvoice(Product product, Bid winning, String actor) {
        Invoice invoice = new Invoice();
        invoice.setInvoiceID(UUID.randomUUID().toString());
        invoice.setAuctionID(product.getAuctionID());
        invoice.setProductID(product.getProductID());
        invoice.setBuyerID(winning.getBidderID());
        invoice.setSellerID(product.getSellerID());
        invoice.setAmount(winning.getAmount());
        invoice.setInvoiceDate(LocalDateTime.now());
        invoice.setPaymentStatus(Invoice.PaymentStatus.PENDING);
        product.setProductStatus(Product.ProductStatus.SOLD);
        product.setUpdatedAt(LocalDateTime.now());
        products.save(product);
        Invoice saved = invoices.saveAndFlush(invoice);
        audit.logAction("INVOICE", saved.getInvoiceID(), "CREATED", actor,
                "product=" + product.getProductID() + ";winningBid=" + winning.getBidID() + ";amountINR=" + saved.getAmount().toPlainString());
        notifications.notifyUser(saved.getBuyerID(), "INVOICE_CREATED", "Your winning auction invoice is ready: " + saved.getInvoiceID(), "/bidder/invoices");
        notifications.notifyUser(saved.getSellerID(), "INVOICE_CREATED", "Your sales invoice is ready: " + saved.getInvoiceID(), "/seller/invoices");
        return toDTO(saved);
    }

    public List<InvoiceDTO> ownInvoices() {
        current.requireApproved();
        return (current.isAdmin() ? invoices.findAll() : invoices.findByParty(current.id())).stream().map(this::toDTO).toList();
    }

    public InvoiceDTO findInvoice(String invoiceID) {
        Invoice invoice = invoices.findById(invoiceID).orElseThrow(() -> new EntityNotFoundException("Invoice not found"));
        requireParty(invoice);
        return toDTO(invoice);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public InvoiceDTO updatePaymentStatus(String invoiceID, String paymentStatus) {
        current.requireRoles(User.Role.ADMIN);
        Invoice invoice = invoices.findForUpdateByInvoiceID(invoiceID).orElseThrow(() -> new EntityNotFoundException("Invoice not found"));
        if (invoice.getPaymentSource() == Invoice.PaymentSource.RAZORPAY || paymentOrders.findByInvoiceID(invoiceID).isPresent())
            throw new IllegalArgumentException("This invoice has an online payment order. Its status is verified by the payment provider.");
        Invoice.PaymentStatus next;
        try {
            next = Invoice.PaymentStatus.valueOf(paymentStatus.toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new IllegalArgumentException("Payment status must be PENDING, PAID or FAILED");
        }
        if (invoice.getPaymentStatus() == Invoice.PaymentStatus.PAID && next != Invoice.PaymentStatus.PAID)
            throw new IllegalArgumentException("A paid invoice cannot be downgraded. Refunds require a separate reviewed workflow.");
        if (invoice.getPaymentStatus() == next) return toDTO(invoice);
        Invoice.PaymentStatus previous = invoice.getPaymentStatus();
        invoice.setPaymentStatus(next);
        invoice.setPaymentSource(Invoice.PaymentSource.MANUAL);
        invoice.setPaymentReference(null);
        invoice.setUpdatedAt(LocalDateTime.now());
        audit.logAction("INVOICE", invoiceID, "MANUAL_PAYMENT_STATUS", current.id(),
                "from=" + previous.name() + ";to=" + next.name());
        return toDTO(invoices.save(invoice));
    }

    public byte[] downloadInvoice(String invoiceID) {
        InvoiceDTO dto = findInvoice(invoiceID);
        List<String> lines = new ArrayList<>();
        lines.add("BidPro - Sales invoice");
        lines.add("Invoice: " + dto.getInvoiceID());
        lines.add("Date: " + dto.getInvoiceDate());
        lines.add("Auction: " + dto.getAuctionID());
        lines.add("");
        lines.add("Item: " + dto.getProductTitle());
        lines.add("Seller: " + dto.getSellerName());
        lines.add("Buyer: " + dto.getBuyerName());
        lines.add("Winning amount (INR): " + dto.getAmount().toPlainString());
        lines.add("Payment status: " + dto.getPaymentStatus());
        lines.add("Payment source: " + dto.getPaymentSource());
        if (dto.getPaymentMode() != null) lines.add("Payment mode: " + dto.getPaymentMode() + (("test".equals(dto.getPaymentMode()) || "local-test".equals(dto.getPaymentMode())) ? " (no real money transferred)" : ""));
        if (dto.getPaymentReference() != null) lines.add("Payment reference: " + dto.getPaymentReference());
        lines.add("");
        lines.add("This invoice records the auction result. It is not proof of payment.");
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                content.beginText();
                content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 11);
                content.setLeading(17);
                content.newLineAtOffset(50, 740);
                for (String line : lines) {
                    // Standard PDF fonts are portable without host font files.
                    // Replace unsupported glyphs rather than failing downloads.
                    String printable = line.replaceAll("[^\\x20-\\x7E]", "?");
                    for (int start = 0; start < Math.max(1, printable.length()); start += 85) {
                        content.showText(printable.substring(start, Math.min(start + 85, printable.length())));
                        content.newLine();
                    }
                }
                content.endText();
            }
            document.save(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Could not create the invoice PDF", exception);
        }
    }

    private void requireParty(Invoice invoice) {
        current.requireApproved();
        String id = current.id();
        if (!current.isAdmin() && !id.equals(invoice.getBuyerID()) && !id.equals(invoice.getSellerID()))
            throw new AccessDeniedException("You can only access invoices for your own purchases or sales");
    }

    public InvoiceDTO toDTO(Invoice invoice) {
        InvoiceDTO dto = new InvoiceDTO(invoice);
        var paymentOrder = paymentOrders.findByInvoiceID(invoice.getInvoiceID());
        dto.setHasOnlinePaymentOrder(paymentOrder.isPresent());
        paymentOrder.ifPresent(order -> dto.setPaymentMode(order.getMode()));
        products.findById(invoice.getProductID()).ifPresent(product -> dto.setProductTitle(product.getTitle()));
        users.findById(invoice.getBuyerID()).ifPresent(user -> dto.setBuyerName(name(user)));
        users.findById(invoice.getSellerID()).ifPresent(user -> dto.setSellerName(name(user)));
        return dto;
    }
    private String name(User user) { return user.getFirstName() + " " + user.getLastName(); }

    public List<Invoice> findInvoicesByAuction(String auctionID) { return invoices.findByAuctionAuctionID(auctionID); }
    public List<Invoice> findInvoicesByBuyer(String buyerID) { return invoices.findByBuyerUserID(buyerID); }
    public List<Invoice> findInvoicesBySeller(String sellerID) { return invoices.findBySellerUserID(sellerID); }
    public List<Invoice> findInvoicesByPaymentStatus(Invoice.PaymentStatus status) { return invoices.findByPaymentStatus(status); }
    public List<Invoice> findInvoicesByDateRange(LocalDateTime start, LocalDateTime end) { return invoices.findByDateRange(start, end); }
}
