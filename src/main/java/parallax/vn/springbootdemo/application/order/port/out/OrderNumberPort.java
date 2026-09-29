package parallax.vn.springbootdemo.application.order.port.out;

/** Generates unique, human-friendly order numbers (backed by a DB sequence). */
public interface OrderNumberPort {
    String next();
}
