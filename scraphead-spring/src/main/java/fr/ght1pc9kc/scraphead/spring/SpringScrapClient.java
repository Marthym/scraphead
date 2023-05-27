package fr.ght1pc9kc.scraphead.spring;

import fr.ght1pc9kc.scraphead.core.http.ScrapClient;
import fr.ght1pc9kc.scraphead.core.http.ScrapRequest;
import fr.ght1pc9kc.scraphead.core.http.ScrapResponse;
import fr.ght1pc9kc.scraphead.core.model.ex.InvalidStatusCodeException;
import fr.ght1pc9kc.scraphead.core.model.ex.UnsupportedContentTypeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.StringDecoder;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.http.HttpHeaders;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

@Slf4j
@Component
public class SpringScrapClient implements ScrapClient {
    private static final int MAX_FRAME_LENGTH = 1024 * 1024;

    private final WebClient webClient;

    public SpringScrapClient(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public Mono<ScrapResponse> send(ScrapRequest request) {
        return webClient.get()
                .uri(request.location())
                .headers(headers -> headers.putAll(request.headers().map()))
                .cookies(cookies -> request.cookies().forEach(c -> cookies.addIfAbsent(c.getName(), c.getValue())))
                .exchangeToFlux(response -> {
                    MediaType contentType = extractContentType(response.headers()).orElse(null);
                    if (!response.statusCode().is2xxSuccessful()) {
                        return Flux.error(() ->
                                new InvalidStatusCodeException("Receive not successful status code " + response.statusCode().value()));
                    }
                    if (contentType != null && !contentType.isCompatibleWith(MediaType.TEXT_HTML)) {
                        return Flux.error(() -> new UnsupportedContentTypeException("Content type " + contentType + "was not supported !"));
                    }
                    long contentLength = response.headers().contentLength().orElse(0);
                    if (contentLength > MAX_FRAME_LENGTH) {
                        return Flux.error(() -> new IllegalStateException("Max response size exceeded (" + contentLength + ") !"));

                    }

                    return getStringDecoder(contentType).decode(
                            response.body(BodyExtractors.toDataBuffers()), ResolvableType.NONE, null, null);

                }).take(1).next()
                .map(str -> new ScrapResponse(
                        200,
                        HttpHeaders.of(Map.of(), (k, v) -> true),
                        Flux.just(ByteBuffer.wrap(str.getBytes(StandardCharsets.UTF_8)))));
    }

    private static Optional<MediaType> extractContentType(ClientResponse.Headers headers) {
        List<String> contentTypes = headers.header(CONTENT_TYPE);
        Optional<String> contentType = (!contentTypes.isEmpty()) ? Optional.of(contentTypes.get(0)) : Optional.empty();
        return contentType.map(ct -> ct.replace(",", ";"))
                .flatMap(ct -> {
                    try {
                        return Optional.of(MediaType.parseMediaType(ct));
                    } catch (Exception e) {
                        log.debug("Unable to parse media type : {}", ct);
                        log.trace("STACKTRACE", e);
                        return Optional.empty();
                    }
                });

    }

    private static StringDecoder getStringDecoder(MediaType mediaType) {
        StringDecoder decoder = StringDecoder.allMimeTypes(List.of("</head>", "<body"), true);
        decoder.setMaxInMemorySize(MAX_FRAME_LENGTH);

        if (mediaType != null) {
            Charset charset = mediaType.getCharset();
            if (charset != null) {
                decoder.setDefaultCharset(charset);
            }
        }
        return decoder;
    }
}
