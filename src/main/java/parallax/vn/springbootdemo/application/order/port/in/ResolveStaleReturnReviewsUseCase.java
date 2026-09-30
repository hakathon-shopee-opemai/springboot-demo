package parallax.vn.springbootdemo.application.order.port.in;

/** Re-scores returns stuck in manual review and closes the ones the risk engine now decides. */
public interface ResolveStaleReturnReviewsUseCase {
    int resolveStaleReviews();
}
