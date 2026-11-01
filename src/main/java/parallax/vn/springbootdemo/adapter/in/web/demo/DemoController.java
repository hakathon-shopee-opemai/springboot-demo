package parallax.vn.springbootdemo.adapter.in.web.demo;

import java.util.Map;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** JWT-protected demo endpoint (the echo hop was retired in favour of real business hops). */
@RestController
@RequestMapping("/api/v1/demo")
public class DemoController {

    @GetMapping("/whoami")
    public Map<String, Object> whoami(@AuthenticationPrincipal Jwt jwt) {
        return Map.of("username", jwt.getSubject(), "issuer", jwt.getIssuer().toString());
    }
}
