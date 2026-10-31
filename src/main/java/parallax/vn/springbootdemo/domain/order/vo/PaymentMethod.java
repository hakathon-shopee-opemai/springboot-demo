package parallax.vn.springbootdemo.domain.order.vo;

import java.util.Locale;
import java.util.Map;

public enum PaymentMethod {
    CARD,
    COD,
    BANK_TRANSFER,
    WALLET;

    /** Common client spellings that mean an existing method. */
    private static final Map<String, PaymentMethod> ALIASES = Map.of(
            "CREDIT_CARD", CARD,
            "DEBIT_CARD", CARD);

    /**
     * Lenient parse for client input: case-insensitive, trims whitespace, accepts aliases.
     *
     * @throws IllegalArgumentException if the value is null/blank or not a known method
     */
    public static PaymentMethod parse(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("paymentMethod is required");
        }
        String key = raw.trim().toUpperCase(Locale.ROOT);
        PaymentMethod alias = ALIASES.get(key);
        return alias != null ? alias : PaymentMethod.valueOf(key);
    }
}
