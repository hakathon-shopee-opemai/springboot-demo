package parallax.vn.springbootdemo.config.client;

import java.net.http.HttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import parallax.vn.springbootdemo.config.properties.RiskClientProperties;

/** Builds the RestClient used for the outbound parallax hop to the risk-service. */
@Configuration
public class RestClientConfig {

    @Bean("riskServiceClient")
    public RestClient riskServiceClient(RiskClientProperties properties,
            HmacSigningInterceptor hmacSigningInterceptor,
            TracePropagationInterceptor tracePropagationInterceptor) {
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)  // uvicorn/h11 has no h2c upgrade; HTTP/2 default breaks the hop
                .connectTimeout(properties.connectTimeout())
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(properties.readTimeout());
        return RestClient.builder()
                .baseUrl(properties.baseUrl())
                .requestFactory(requestFactory)
                .requestInterceptor(tracePropagationInterceptor)
                .requestInterceptor(hmacSigningInterceptor)
                .build();
    }
}
