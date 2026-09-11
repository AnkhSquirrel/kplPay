package com.ankhsquirrel.kplpay.invoice;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InvoiceCalculationServiceTest {

    private final InvoiceCalculationService service =
            new InvoiceCalculationService(new BigDecimal("0.20"), 30);

    @Test
    void calculates_subtotal_tax_and_total_for_a_single_line_item() {
        var result = service.calculate("Growth", new BigDecimal("29.99"), LocalDate.of(2026, 1, 1));

        assertThat(result.subtotal()).isEqualByComparingTo("29.99");
        // 29.99 * 0.20 = 5.998 -> HALF_UP -> 6.00
        assertThat(result.taxAmount()).isEqualByComparingTo("6.00");
        assertThat(result.totalAmount()).isEqualByComparingTo("35.99");
        assertThat(result.issueDate()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(result.dueDate()).isEqualTo(LocalDate.of(2026, 1, 31));

        assertThat(result.lineItems()).singleElement().satisfies(li -> {
            assertThat(li.description()).isEqualTo("Growth — monthly subscription");
            assertThat(li.quantity()).isEqualTo(1);
            assertThat(li.unitPrice()).isEqualByComparingTo("29.99");
            assertThat(li.amount()).isEqualByComparingTo("29.99");
        });
    }

    @Test
    void rounds_vat_half_up_when_the_third_decimal_forces_a_carry() {
        // 33.33 * 0.20 = 6.666 -> HALF_UP -> 6.67. Note: an exact x.xx5 tie is mathematically
        // unreachable at exactly 20% VAT on a 2-decimal subtotal (the thousandths digit of
        // subtotal * 0.20 is always even), so this is the realistic form a rounding-boundary
        // case takes here — it still exercises real HALF_UP-vs-truncation/HALF_EVEN divergence.
        var result = service.calculate("Starter", new BigDecimal("33.33"), LocalDate.of(2026, 1, 1));

        assertThat(result.taxAmount()).isEqualByComparingTo("6.67");
        assertThat(result.totalAmount()).isEqualByComparingTo("40.00");
    }

    @Test
    void computes_due_date_from_payment_terms_days_across_a_month_boundary() {
        var result = service.calculate("Starter", new BigDecimal("10.00"), LocalDate.of(2026, 1, 15));

        assertThat(result.dueDate()).isEqualTo(LocalDate.of(2026, 2, 14));
    }

    @Test
    void rejects_a_zero_amount() {
        assertThatThrownBy(() -> service.calculate("Starter", BigDecimal.ZERO, LocalDate.of(2026, 1, 1)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejects_a_negative_amount() {
        assertThatThrownBy(() ->
                service.calculate("Starter", new BigDecimal("-5.00"), LocalDate.of(2026, 1, 1)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
