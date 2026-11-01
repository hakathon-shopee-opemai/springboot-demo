package parallax.vn.springbootdemo.adapter.in.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import parallax.vn.springbootdemo.application.order.port.in.ResolveStaleReturnReviewsUseCase;

/** Driving adapter: periodically closes returns stuck in manual review. */
@Component
public class ReturnReviewScheduler {

    private final ResolveStaleReturnReviewsUseCase resolveStaleReviews;

    public ReturnReviewScheduler(ResolveStaleReturnReviewsUseCase resolveStaleReviews) {
        this.resolveStaleReviews = resolveStaleReviews;
    }

    @Scheduled(fixedDelayString = "${returns.review-sweep-ms:300000}", initialDelay = 60_000)
    public void sweep() {
        resolveStaleReviews.resolveStaleReviews();
    }
}
