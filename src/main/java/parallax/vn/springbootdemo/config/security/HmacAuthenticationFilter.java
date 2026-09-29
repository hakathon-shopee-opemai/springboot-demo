package parallax.vn.springbootdemo.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import parallax.vn.springbootdemo.common.exception.code.ErrorCode;
import parallax.vn.springbootdemo.common.exception.model.ProblemDetails;
import parallax.vn.springbootdemo.common.util.CachedBodyHttpServletRequest;
import parallax.vn.springbootdemo.common.util.HmacSigner;
import parallax.vn.springbootdemo.config.properties.SecurityProperties;

/** Verifies the HMAC signature on internal (service-to-service) routes. */
public class HmacAuthenticationFilter extends OncePerRequestFilter {

    private static final long MAX_SKEW_SECONDS = 300;

    private final HmacSigner signer;
    private final SecurityProperties properties;
    private final ObjectMapper objectMapper;

    public HmacAuthenticationFilter(HmacSigner signer, SecurityProperties properties, ObjectMapper objectMapper) {
        this.signer = signer;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        CachedBodyHttpServletRequest wrapped = new CachedBodyHttpServletRequest(request);

        String signature = request.getHeader("X-Signature");
        String ts = request.getHeader("X-Signature-Ts");
        if (!StringUtils.hasText(signature) || !StringUtils.hasText(ts)) {
            reject(response, ErrorCode.UNAUTHENTICATED, "Missing signature headers");
            return;
        }

        long tsValue;
        try {
            tsValue = Long.parseLong(ts.trim());
        } catch (NumberFormatException e) {
            reject(response, ErrorCode.SIGNATURE_INVALID, "Invalid timestamp");
            return;
        }
        if (Math.abs(Instant.now().getEpochSecond() - tsValue) > MAX_SKEW_SECONDS) {
            reject(response, ErrorCode.SIGNATURE_INVALID, "Timestamp outside allowed skew");
            return;
        }

        String canonical = signer.canonical(request.getMethod(), request.getRequestURI(), ts,
                wrapped.getCachedBody());
        if (!signer.verify(properties.serviceSharedSecret(), canonical, signature)) {
            reject(response, ErrorCode.SIGNATURE_INVALID, "Signature verification failed");
            return;
        }

        var authentication = new UsernamePasswordAuthenticationToken(
                "service", null, List.of(new SimpleGrantedAuthority("ROLE_SERVICE")));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(wrapped, response);
    }

    private void reject(HttpServletResponse response, ErrorCode code, String detail) throws IOException {
        response.setStatus(code.getStatus().value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), ProblemDetails.asMap(code, detail));
    }
}
