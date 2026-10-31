package parallax.vn.springbootdemo.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import parallax.vn.springbootdemo.domain.order.vo.PaymentMethod;

class PaymentMethodTest {

    @Test
    void parses_canonical_names() {
        for (PaymentMethod m : PaymentMethod.values()) {
            assertThat(PaymentMethod.parse(m.name())).isEqualTo(m);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"CREDIT_CARD", "DEBIT_CARD", "credit_card", " Credit_Card "})
    void card_aliases_map_to_card(String raw) {
        assertThat(PaymentMethod.parse(raw)).isEqualTo(PaymentMethod.CARD);
    }

    @Test
    void is_case_insensitive() {
        assertThat(PaymentMethod.parse("cod")).isEqualTo(PaymentMethod.COD);
    }

    @ParameterizedTest
    @ValueSource(strings = {"BITCOIN", "", "  "})
    void rejects_unknown_or_blank(String raw) {
        assertThatThrownBy(() -> PaymentMethod.parse(raw)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejects_null() {
        assertThatThrownBy(() -> PaymentMethod.parse(null)).isInstanceOf(IllegalArgumentException.class);
    }
}
