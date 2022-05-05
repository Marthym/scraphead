package fr.ght1pc9kc.scraphead.spring.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.Http11SslContextSpec;
import reactor.netty.http.client.HttpClient;

import java.util.Set;

@Configuration
@SuppressWarnings("SpringFacetCodeInspection")
public class ScrapheadWebClientConfiguration {

    @Bean
    @ConditionalOnMissingBean
    WebClient scrapheadWebclient() {
        return WebClient.builder().clientConnector(new ReactorClientHttpConnector(
                HttpClient.create()
                        .secure(spec -> spec.sslContext(Http11SslContextSpec.forClient()))
                        .followRedirect(true)
                        .followRedirect((req, res) -> // 303 was not in the default code
                                Set.of(301, 302, 303, 307, 308).contains(res.status().code()))
                        .compress(true)
        )).build();
    }
}
