package parallax.vn.springbootdemo.adapter.out.persistence.order.adapter;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import org.springframework.jdbc.core.JdbcTemplate;
import parallax.vn.springbootdemo.application.order.port.out.OrderNumberPort;
import parallax.vn.springbootdemo.common.annotation.PersistenceAdapter;

/** Produces ORD-yyyyMMdd-###### using a Postgres sequence. */
@PersistenceAdapter
public class OrderNumberAdapter implements OrderNumberPort {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final JdbcTemplate jdbcTemplate;

    public OrderNumberAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public String next() {
        Long seq = jdbcTemplate.queryForObject("SELECT nextval('order_number_seq')", Long.class);
        long value = seq == null ? 0L : seq;
        String date = LocalDate.now(ZoneOffset.UTC).format(DATE);
        return String.format("ORD-%s-%06d", date, value % 1_000_000);
    }
}
