package parallax.vn.springbootdemo.domain.order.vo;

/** One scoring rule that fired, kept for explainability. */
public record RiskFactor(String code, String description, int weight, int contribution) {
}
