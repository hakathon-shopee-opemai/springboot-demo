package parallax.vn.springbootdemo.application.customer.dto;

public record CreateCustomerCommand(
        String email,
        String fullName,
        String phone,
        String tier,
        String accountStatus) {
}
