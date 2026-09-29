package parallax.vn.springbootdemo.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.AuthenticationEntryPoint;
import parallax.vn.springbootdemo.common.exception.code.ErrorCode;
import parallax.vn.springbootdemo.common.exception.model.ProblemDetails;
import parallax.vn.springbootdemo.common.util.HmacSigner;
import parallax.vn.springbootdemo.config.properties.RateLimitProperties;
import parallax.vn.springbootdemo.config.properties.SecurityProperties;

/**
 * Two chains: (1) /internal/** authenticated by HMAC; (2) everything else — public login +
 * health open, all other /api/v1/** requires a Bearer JWT, guarded by the rate limiter.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final HmacSigner signer;
    private final SecurityProperties securityProperties;
    private final RateLimitProperties rateLimitProperties;
    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    public SecurityConfig(HmacSigner signer, SecurityProperties securityProperties,
            RateLimitProperties rateLimitProperties, StringRedisTemplate redis, ObjectMapper objectMapper) {
        this.signer = signer;
        this.securityProperties = securityProperties;
        this.rateLimitProperties = rateLimitProperties;
        this.redis = redis;
        this.objectMapper = objectMapper;
    }

    @Bean
    @Order(1)
    public SecurityFilterChain internalChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/internal/**")
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a -> a.anyRequest().authenticated())
                .addFilterBefore(new HmacAuthenticationFilter(signer, securityProperties, objectMapper),
                        AuthorizationFilter.class)
                .exceptionHandling(e -> e.authenticationEntryPoint(authenticationEntryPoint()));
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain apiChain(HttpSecurity http, JwtDecoder jwtDecoder) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/api/v1/auth/login").permitAll()
                        .requestMatchers("/actuator/health/**", "/actuator/info", "/error").permitAll()
                        .requestMatchers("/api/v1/**").authenticated()
                        .anyRequest().permitAll())
                .oauth2ResourceServer(o -> o.jwt(Customizer.withDefaults()))
                .addFilterBefore(new RateLimitFilter(redis, rateLimitProperties, objectMapper),
                        AuthorizationFilter.class)
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler()));
        return http.build();
    }

    private AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, ex) -> {
            response.setStatus(ErrorCode.UNAUTHENTICATED.getStatus().value());
            response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
            objectMapper.writeValue(response.getOutputStream(),
                    ProblemDetails.asMap(ErrorCode.UNAUTHENTICATED, "Authentication required"));
        };
    }

    private AccessDeniedHandler accessDeniedHandler() {
        return (request, response, ex) -> {
            response.setStatus(ErrorCode.ACCESS_DENIED.getStatus().value());
            response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
            objectMapper.writeValue(response.getOutputStream(),
                    ProblemDetails.asMap(ErrorCode.ACCESS_DENIED, "Access denied"));
        };
    }
}
