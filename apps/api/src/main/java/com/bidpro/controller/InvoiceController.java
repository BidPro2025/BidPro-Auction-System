package com.bidpro.controller;

import com.bidpro.dto.ApiResponse;
import com.bidpro.dto.InvoiceDTO;
import com.bidpro.service.InvoiceService;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {
    private final InvoiceService invoices;
    public InvoiceController(InvoiceService invoices) { this.invoices = invoices; }

    @PostMapping
    public ResponseEntity<ApiResponse<InvoiceDTO>> createInvoice(@RequestParam String productID) {
        return ResponseEntity.ok(ApiResponse.success(invoices.createInvoice(productID), "Invoice generated successfully"));
    }
    @GetMapping({"", "/mine"})
    public ResponseEntity<ApiResponse<List<InvoiceDTO>>> ownInvoices() {
        return ResponseEntity.ok(ApiResponse.success(invoices.ownInvoices(), "Invoices retrieved successfully"));
    }
    @GetMapping("/{invoiceID}")
    public ResponseEntity<ApiResponse<InvoiceDTO>> findInvoice(@PathVariable String invoiceID) {
        return ResponseEntity.ok(ApiResponse.success(invoices.findInvoice(invoiceID), "Invoice retrieved successfully"));
    }
    @GetMapping("/{invoiceID}/download")
    public ResponseEntity<byte[]> download(@PathVariable String invoiceID) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"bidpro-invoice-" + invoiceID.replaceAll("[^a-zA-Z0-9-]", "") + ".pdf\"")
                .cacheControl(CacheControl.noStore()).body(invoices.downloadInvoice(invoiceID));
    }
    @PutMapping("/{invoiceID}/payment-status")
    public ResponseEntity<ApiResponse<InvoiceDTO>> updatePaymentStatus(@PathVariable String invoiceID,
            @RequestParam(required = false) String paymentStatus, @RequestBody(required = false) Map<String, String> body) {
        String status = paymentStatus != null ? paymentStatus : body == null ? null : body.get("paymentStatus");
        return ResponseEntity.ok(ApiResponse.success(invoices.updatePaymentStatus(invoiceID, status), "Payment status updated successfully"));
    }
}
