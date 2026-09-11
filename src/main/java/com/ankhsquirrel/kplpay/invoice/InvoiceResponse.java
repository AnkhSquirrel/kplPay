package com.ankhsquirrel.kplpay.invoice;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * API view of an {@link Invoice}, with its {@link LineItem}s nested inline — a line item has no
 * independent existence or API identity outside its invoice.
 */
public record InvoiceResponse(
        UUID id,
        UUID customerId,
        UUID subscriptionId,
        LocalDate issueDate,
        LocalDate dueDate,
        InvoiceStatus status,
        BigDecimal subtotal,
        BigDecimal taxAmount,
        BigDecimal totalAmount,
        List<LineItemResponse> lineItems,
        LocalDateTime createdAt) {

    public static InvoiceResponse from(Invoice invoice) {
        return new InvoiceResponse(
                invoice.getId(),
                invoice.getCustomerId(),
                invoice.getSubscriptionId(),
                invoice.getIssueDate(),
                invoice.getDueDate(),
                invoice.getStatus(),
                invoice.getSubtotal(),
                invoice.getTaxAmount(),
                invoice.getTotalAmount(),
                invoice.getLineItems().stream().map(LineItemResponse::from).toList(),
                invoice.getCreatedAt());
    }

    public record LineItemResponse(
            UUID id,
            String description,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal amount) {

        public static LineItemResponse from(LineItem lineItem) {
            return new LineItemResponse(
                    lineItem.getId(),
                    lineItem.getDescription(),
                    lineItem.getQuantity(),
                    lineItem.getUnitPrice(),
                    lineItem.getAmount());
        }
    }
}
