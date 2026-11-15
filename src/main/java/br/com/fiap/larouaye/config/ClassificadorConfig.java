package br.com.fiap.larouaye.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/** Cliente HTTP do serviço Flask (classificador de ML), com tempo limite curto para nunca travar a API. */
@Configuration
public class ClassificadorConfig {

    @Bean
    public RestClient classificadorRestClient(
            @Value("${larouaye.classificador.url}") String url,
            @Value("${larouaye.classificador.timeout-ms}") int timeoutMs) {
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(Duration.ofMillis(timeoutMs));
        fabrica.setReadTimeout(Duration.ofMillis(timeoutMs));
        return RestClient.builder().baseUrl(url).requestFactory(fabrica).build();
    }
}
