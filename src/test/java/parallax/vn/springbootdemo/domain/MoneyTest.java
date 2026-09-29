package parallax.vn.springbootdemo.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import parallax.vn.springbootdemo.domain.shared.vo.Money;

class MoneyTest {

    @Test
    void adds_same_currency_and_scales_to_two_decimals() {
        Money sum = Money.of("10.1", "USD").add(Money.of("5", "USD"));
        assertThat(sum.amount()).isEqualByComparingTo(new BigDecimal("15.10"));
        assertThat(sum.currency()).isEqualTo("USD");
    }

    @Test
    void multiply_scales_by_quantity() {
        assertThat(Money.of("3000.00", "USD").multiply(2).amount())
                .isEqualByComparingTo(new BigDecimal("6000.00"));
    }

    @Test
    void rejects_currency_mismatch() {
        assertThatThrownBy(() -> Money.of("1", "USD").add(Money.of("1", "EUR")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
