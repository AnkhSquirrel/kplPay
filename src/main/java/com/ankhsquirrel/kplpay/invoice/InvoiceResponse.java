package com.ankhsquirrel.kplpay.invoice;

import io.swagger.v3.oas.annotations.media.Schema;

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

        @Schema(description = "Invoice identifier")
        UUID id,

        @Schema(description = "Identifier of the billed customer")
        UUID customerId,

        @Schema(description = "Identifier of the subscription this invoice was generated from")
        UUID subscriptionId,

        @Schema(description = "Date the invoice was issued")
        LocalDate issueDate,

        @Schema(description = "Payment due date")
        LocalDate dueDate,

        InvoiceStatus status,

        @Schema(description = "Total before VAT", example = "49.90")
        BigDecimal subtotal,

        @Schema(description = "VAT amount", example = "9.98")
        BigDecimal taxAmount,

        @Schema(description = "Total including VAT", example = "59.88")
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

            @Schema(description = "Line item identifier")
            UUID id,

            @Schema(description = "What this line represents", example = "Growth — monthly subscription")
            String description,

            Integer quantity,

            @Schema(description = "Price per unit, excluding VAT", example = "49.90")
            BigDecimal unitPrice,

            @Schema(description = "quantity * unitPrice", example = "49.90")
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
