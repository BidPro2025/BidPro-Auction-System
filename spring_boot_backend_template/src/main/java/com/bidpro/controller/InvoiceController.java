package com.bidpro.controller;

import com.bidpro.dto.ApiResponse;
import com.bidpro.dto.InvoiceDTO;
import com.bidpro.service.InvoiceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    @PostMapping
    public ResponseEntity<ApiResponse<InvoiceDTO>> createInvoice(@RequestParam String productID, Principal principal) {
        String buyerID = principal.getName();
        InvoiceDTO invoice = invoiceService.createInvoice(productID, buyerID);
        return ResponseEntity.ok(ApiResponse.success(invoice, "Invoice created successfully"));
    }

    @PutMapping("/{invoiceID}/payment-status")
    public ResponseEntity<ApiResponse<InvoiceDTO>> updatePaymentStatus(@PathVariable String invoiceID, 
            @RequestParam String paymentStatus) {
        InvoiceDTO updatedInvoice = invoiceService.updatePaymentStatus(invoiceID, paymentStatus);
        return ResponseEntity.ok(ApiResponse.success(updatedInvoice, "Payment status updated successfully"));
    }
}