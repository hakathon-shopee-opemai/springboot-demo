package parallax.vn.springbootdemo.common.logging.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Logs entry/exit/duration for anything annotated with {@code @Loggable}. */
@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    @Pointcut("@annotation(parallax.vn.springbootdemo.common.annotation.Loggable) "
            + "|| @within(parallax.vn.springbootdemo.common.annotation.Loggable)")
    public void loggable() {
    }

    @Around("loggable()")
    public Object logAround(ProceedingJoinPoint pjp) throws Throwable {
        String sig = pjp.getSignature().toShortString();
        long start = System.nanoTime();
        log.debug("-> {}", sig);
        try {
            Object result = pjp.proceed();
            log.debug("<- {} ({} ms)", sig, (System.nanoTime() - start) / 1_000_000);
            return result;
        } catch (Throwable ex) {
            log.warn("x  {} failed: {}", sig, ex.toString());
            throw ex;
        }
    }
}
