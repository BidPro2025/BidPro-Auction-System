package com.bidpro.service;

import com.bidpro.dao.InvoiceDao;
import com.bidpro.dao.ProductDao;
import com.bidpro.dao.UserDao;
import com.bidpro.dto.InvoiceDTO;
import com.bidpro.entities.Invoice;
import com.bidpro.entities.Product;
import com.bidpro.entities.User;
import com.bidpro.custom_exceptions.InvalidInputException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class InvoiceService {

    @Autowired
    private InvoiceDao invoiceDao;

    @Autowired
    private ProductDao productDao;

    @Autowired
    private UserDao userDao;

    @Transactional
    public InvoiceDTO createInvoice(String productID, String buyerID) {
        Product product = productDao.findById(productID)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with ID: " + productID));
        User buyer = userDao.findById(buyerID)
                .orElseThrow(() -> new EntityNotFoundException("Buyer not found with ID: " + buyerID));
        User seller = product.getSeller();
        if (seller == null) {
            throw new InvalidInputException("Seller not found for product ID: " + productID);
        }

        Invoice invoice = new Invoice();
        invoice.setInvoiceID(UUID.randomUUID().toString());
        invoice.setProduct(product);
        invoice.setBuyer(buyer);
        invoice.setSeller(seller);
        invoice.setAmount(product.getStartingPrice());
        invoice.setInvoiceDate(LocalDateTime.now());
        invoice.setPaymentStatus(Invoice.PaymentStatus.PENDING);

        invoice = invoiceDao.save(invoice);

        InvoiceDTO dto = new InvoiceDTO();
        dto.setInvoiceID(invoice.getInvoiceID());
        dto.setAuctionID(invoice.getAuction() != null ? invoice.getAuction().getAuctionID() : null);
        dto.setProductID(productID);
        dto.setBuyerID(buyerID);
        dto.setSellerID(seller.getUserID());
        dto.setAmount(invoice.getAmount());
        dto.setInvoiceDate(invoice.getInvoiceDate());
        dto.setPaymentStatus(invoice.getPaymentStatus());
        return dto;
    }

    @Transactional
    public InvoiceDTO updatePaymentStatus(String invoiceID, String paymentStatus) {
        Invoice invoice = invoiceDao.findById(invoiceID)
                .orElseThrow(() -> new EntityNotFoundException("Invoice not found with ID: " + invoiceID));
        try {
            invoice.setPaymentStatus(Invoice.PaymentStatus.valueOf(paymentStatus));
        } catch (IllegalArgumentException e) {
            throw new InvalidInputException("Invalid payment status: " + paymentStatus);
        }
        invoice = invoiceDao.save(invoice);

        InvoiceDTO dto = new InvoiceDTO();
        dto.setInvoiceID(invoice.getInvoiceID());
        dto.setAuctionID(invoice.getAuction() != null ? invoice.getAuction().getAuctionID() : null);
        dto.setProductID(invoice.getProduct().getProductID());
        dto.setBuyerID(invoice.getBuyer().getUserID());
        dto.setSellerID(invoice.getSeller().getUserID());
        dto.setAmount(invoice.getAmount());
        dto.setInvoiceDate(invoice.getInvoiceDate());
        dto.setPaymentStatus(invoice.getPaymentStatus());
        return dto;
    }

    public List<Invoice> findInvoicesByAuction(String auctionID) {
        return invoiceDao.findByAuctionAuctionID(auctionID);
    }

    public List<Invoice> findInvoicesByBuyer(String buyerID) {
        return invoiceDao.findByBuyerUserID(buyerID);
    }

    public List<Invoice> findInvoicesBySeller(String sellerID) {
        return invoiceDao.findBySellerUserID(sellerID);
    }

    public List<Invoice> findInvoicesByPaymentStatus(Invoice.PaymentStatus paymentStatus) {
        return invoiceDao.findByPaymentStatus(paymentStatus);
    }

    public List<Invoice> findInvoicesByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return invoiceDao.findByDateRange(startDate, endDate);
    }
}