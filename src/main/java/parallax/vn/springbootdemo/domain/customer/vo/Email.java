package parallax.vn.springbootdemo.domain.customer.vo;

import java.util.Objects;

public record Email(String value) {
    public Email {
        Objects.requireNonNull(value, "email");
        if (!value.contains("@") || value.length() > 320) {
            throw new IllegalArgumentException("invalid email: " + value);
        }
    }

    public static Email of(String value) {
        return new Email(value);
    }
}
