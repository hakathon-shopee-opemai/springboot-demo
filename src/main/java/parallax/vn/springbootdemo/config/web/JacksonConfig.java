package parallax.vn.springbootdemo.config.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Boot 4 auto-configures the Jackson 3 ({@code tools.jackson}) mapper for web, and no
 * longer exposes a Jackson 2 {@code com.fasterxml.jackson.databind.ObjectMapper} bean. Our
 * cross-cutting code (Redis serialization, problem+json in filters, cache manager) is written
 * against Jackson 2, so we provide that bean explicitly here.
 */
@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}
