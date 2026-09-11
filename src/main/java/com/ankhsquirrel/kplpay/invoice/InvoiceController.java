package com.ankhsquirrel.kplpay.invoice;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final InvoiceService service;

    public InvoiceController(InvoiceService service) {
        this.service = service;
    }

    @PostMapping("/generate/{subscriptionId}")
    public ResponseEntity<InvoiceResponse> generate(@PathVariable UUID subscriptionId) {
        InvoiceResponse body = service.generate(subscriptionId);

        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/invoices/{id}")
                .buildAndExpand(body.id())
                .toUri();
        return ResponseEntity.created(location).body(body);
    }
}
