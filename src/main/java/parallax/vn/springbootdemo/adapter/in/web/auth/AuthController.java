package parallax.vn.springbootdemo.adapter.in.web.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import parallax.vn.springbootdemo.common.exception.code.ErrorCode;
import parallax.vn.springbootdemo.common.exception.model.ApplicationException;
import parallax.vn.springbootdemo.config.properties.SecurityProperties;
import parallax.vn.springbootdemo.config.security.JwtTokenService;

/** Demo login: validates the configured demo user (BCrypt) and issues a JWT. */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final JwtTokenService tokenService;
    private final SecurityProperties properties;
    private final PasswordEncoder passwordEncoder;
    private final String encodedPassword;

    public AuthController(JwtTokenService tokenService, SecurityProperties properties,
            PasswordEncoder passwordEncoder) {
        this.tokenService = tokenService;
        this.properties = properties;
        this.passwordEncoder = passwordEncoder;
        this.encodedPassword = passwordEncoder.encode(properties.demoUser().password());
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        boolean userOk = constantTimeEquals(request.username(), properties.demoUser().username());
        boolean passwordOk = passwordEncoder.matches(request.password(), encodedPassword);
        if (!userOk || !passwordOk) {
            throw new ApplicationException(ErrorCode.UNAUTHENTICATED, "Invalid credentials");
        }
        String token = tokenService.issue(request.username());
        return new LoginResponse(token, "Bearer", tokenService.ttlSeconds());
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
    }

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {
    }

    public record LoginResponse(String accessToken, String tokenType, long expiresIn) {
    }
}
