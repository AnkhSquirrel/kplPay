package com.ankhsquirrel.kplpay.invoice;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * A generated bill for one subscription's billing cycle.
 *
 * <p>References its customer and subscription by id only ({@code customerId},
 * {@code subscriptionId}), following the same aggregate-boundary convention as
 * {@link com.ankhsquirrel.kplpay.subscription.Subscription#getCustomerId()}.
 *
 * <p>{@code lineItems} IS a real JPA association (unlike the id-only references above) because
 * line items have no independent existence outside their invoice: the DB enforces this with
 * {@code ON DELETE CASCADE} on {@code line_item.invoice_id}, and cascade/orphanRemoval here keeps
 * the JPA side consistent with that domain rule.
 */
@Entity
@Table(name = "invoice")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // required by JPA
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private UUID id;

    @Column(name = "customer_id", nullable = false, updatable = false)
    private UUID customerId;

    @Column(name = "subscription_id", nullable = false, updatable = false)
    private UUID subscriptionId;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private InvoiceStatus status;

    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "tax_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    // Getter/setter excluded from Lombok: lineItems must only be mutated via addLineItem(), so
    // both sides of the association stay in sync. See the hand-written getter below.
    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private List<LineItem> lineItems = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private LocalDateTime createdAt;

    public Invoice(UUID customerId, UUID subscriptionId, LocalDate issueDate, LocalDate dueDate,
                    BigDecimal subtotal, BigDecimal taxAmount, BigDecimal totalAmount) {
        this.customerId = customerId;
        this.subscriptionId = subscriptionId;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.status = InvoiceStatus.DRAFT;
        this.subtotal = subtotal;
        this.taxAmount = taxAmount;
        this.totalAmount = totalAmount;
    }

    /** Keeps both sides of the association in sync. */
    public void addLineItem(LineItem lineItem) {
        lineItems.add(lineItem);
        lineItem.setInvoice(this);
    }

    /** Unmodifiable view — mutate via {@link #addLineItem(LineItem)}, never this list directly. */
    public List<LineItem> getLineItems() {
        return Collections.unmodifiableList(lineItems);
    }
}
