package fr.ght1pc9kc.scraphead.spring;

import fr.ght1pc9kc.scraphead.core.http.ScrapClient;
import fr.ght1pc9kc.scraphead.core.http.ScrapRequest;
import fr.ght1pc9kc.scraphead.core.http.ScrapResponse;
import fr.ght1pc9kc.scraphead.core.model.ex.InvalidStatusCodeException;
import fr.ght1pc9kc.scraphead.core.model.ex.UnsupportedContentTypeException;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.StringDecoder;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.http.HttpHeaders;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class SpringScrapClient implements ScrapClient {

    private final WebClient webClient;
    private final StringDecoder responseDecoder;

    public SpringScrapClient(WebClient webClient) {
        this.webClient = webClient;
        this.responseDecoder = StringDecoder.textPlainOnly(List.of("\"</head>\"", "<body"), true);
        this.responseDecoder.setMaxInMemorySize(16 * 1024 * 1024);
    }

    @Override
    public Mono<ScrapResponse> send(ScrapRequest request) {
        return webClient.get()
                .uri(request.location())
                .headers(headers -> headers.putAll(request.headers().map()))
                .cookies(cookies -> request.cookies().forEach(c -> cookies.addIfAbsent(c.getName(), c.getValue())))
                .exchangeToMono(response -> {
                    MediaType contentTYpe = response.headers().contentType().orElse(null);
                    if (!response.statusCode().is2xxSuccessful()) {
                        return Mono.error(() ->
                                new InvalidStatusCodeException("Receive not successful status code " + response.rawStatusCode()));
                    }
                    if (contentTYpe != null && !contentTYpe.isCompatibleWith(MediaType.TEXT_HTML)) {
                        return Mono.error(() -> new UnsupportedContentTypeException("Content type " + contentTYpe + "was not supported !"));
                    }

                    Flux<ByteBuffer> bufferFlux = responseDecoder.decode(
                            response.bodyToFlux(DataBuffer.class), ResolvableType.NONE, contentTYpe, null
                    ).take(1).map(str -> ByteBuffer.wrap(str.getBytes(StandardCharsets.UTF_8)));

                    ScrapResponse scrapResponse = new ScrapResponse(
                            response.rawStatusCode(),
                            HttpHeaders.of(response.headers().asHttpHeaders(), (k, v) -> true),
                            bufferFlux);
                    return Mono.just(scrapResponse);
                });
    }
}
