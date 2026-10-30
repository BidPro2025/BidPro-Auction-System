package com.bidpro.service;

import com.bidpro.dao.ProductDao;
import com.bidpro.entities.Product;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

/** Bounded candidate selection; each invoice is committed in its own transaction. */
@Service
public class AuctionSettlementService {
    private final ProductDao products;
    private final InvoiceService invoices;

    public AuctionSettlementService(ProductDao products, InvoiceService invoices) {
        this.products = products;
        this.invoices = invoices;
    }

    public List<String> ready() {
        return products.findSettlementCandidates(Product.ProductStatus.APPROVED,
                LocalDateTime.now(), PageRequest.of(0, 50));
    }

    public void settle(String productID) {
        invoices.settleExpiredAuction(productID);
    }
}
