package fr.ght1pc9kc.scraphead.spring;

import fr.ght1pc9kc.scraphead.core.http.ScrapClient;
import fr.ght1pc9kc.scraphead.core.http.ScrapRequest;
import fr.ght1pc9kc.scraphead.core.http.ScrapResponse;
import fr.ght1pc9kc.scraphead.core.model.ex.InvalidStatusCodeException;
import fr.ght1pc9kc.scraphead.core.model.ex.UnsupportedContentTypeException;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.StringDecoder;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.http.HttpHeaders;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Component
public class SpringScrapClient implements ScrapClient {
    private static final int MAX_FRAME_LENGTH = 1024 * 1024;

    private final WebClient webClient;
    private final StringDecoder responseDecoder;

    public SpringScrapClient(WebClient webClient) {
        this.webClient = webClient;
        this.responseDecoder = StringDecoder.allMimeTypes(List.of("</head>", "<body"), true);
        this.responseDecoder.setMaxInMemorySize(MAX_FRAME_LENGTH);
    }

    @Override
    public Mono<ScrapResponse> send(ScrapRequest request) {
        return webClient.get()
                .uri(request.location())
                .headers(headers -> headers.putAll(request.headers().map()))
                .cookies(cookies -> request.cookies().forEach(c -> cookies.addIfAbsent(c.getName(), c.getValue())))
                .exchangeToFlux(response -> {
                    MediaType contentType = response.headers().contentType().orElse(null);
                    if (!response.statusCode().is2xxSuccessful()) {
                        return Flux.error(() ->
                                new InvalidStatusCodeException("Receive not successful status code " + response.rawStatusCode()));
                    }
                    if (contentType != null && !contentType.isCompatibleWith(MediaType.TEXT_HTML)) {
                        return Flux.error(() -> new UnsupportedContentTypeException("Content type " + contentType + "was not supported !"));
                    }
                    long contentLength = response.headers().contentLength().orElse(0);
                    if (contentLength > MAX_FRAME_LENGTH) {
                        return Flux.error(() -> new IllegalStateException("Max response size exceeded (" + contentLength + ") !"));

                    }

                    return responseDecoder.decode(
                            response.body(BodyExtractors.toDataBuffers()), ResolvableType.NONE, null, null);

                }).take(1).next()
                .map(str -> new ScrapResponse(
                        200,
                        HttpHeaders.of(Map.of(), (k, v) -> true),
                        Flux.just(ByteBuffer.wrap(str.getBytes(StandardCharsets.UTF_8)))));
    }
}
