package com.ankhsquirrel.kplpay.subscription;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A customer's ongoing commitment to a plan at a fixed monthly amount. Each billing cycle,
 * kplPay turns an active subscription into an invoice.
 *
 * <p>A subscription references its customer by id only ({@code customerId}), not through a JPA
 * association: customer and subscription are separate aggregates. The service checks the
 * customer exists at creation time; the {@code customer_id} foreign key enforces referential
 * integrity thereafter.
 */
@Entity
@Table(name = "subscription")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // required by JPA
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private UUID id;

    @Column(name = "customer_id", nullable = false, updatable = false)
    private UUID customerId;

    @Column(name = "plan_name", nullable = false, length = 100)
    private String planName;

    @Column(name = "monthly_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal monthlyAmount;

    /**
     * Day of the month the subscription is billed on, 1-28. Capped at 28 (not 31) so every
     * subscription has a valid billing date in every month, February included. Deliberate
     * domain constraint, mirrored by the DB CHECK and by Bean Validation on the request DTO.
     */
    @Column(name = "billing_cycle_day", nullable = false)
    @JdbcTypeCode(SqlTypes.SMALLINT) // column is SMALLINT; keep the Java type Integer
    private Integer billingCycleDay;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SubscriptionStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private LocalDateTime createdAt;

    public Subscription(UUID customerId, String planName, BigDecimal monthlyAmount, Integer billingCycleDay) {
        this.customerId = customerId;
        this.planName = planName;
        this.monthlyAmount = monthlyAmount;
        this.billingCycleDay = billingCycleDay;
        this.status = SubscriptionStatus.ACTIVE;
    }
}
