package fr.ght1pc9kc.scraphead.netty.http;


import fr.ght1pc9kc.scraphead.core.http.ScrapClient;
import fr.ght1pc9kc.scraphead.core.http.ScrapRequest;
import fr.ght1pc9kc.scraphead.core.http.ScrapResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.netty.http.client.HttpClient;

import java.net.HttpCookie;
import java.net.http.HttpHeaders;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Netty Reactor implementation for {@link ScrapClient}.
 * <strong>Feature:</strong>
 * <ul>
 *     <li>SSL enable</li>
 *     <li>Auto-redirect (including 303)</li>
 *     <li>Compression enable</li>
 *     <li>Download only HTML {@code <head/>} from page</li>
 * </ul>
 */
@Slf4j
public class NettyScrapClient implements ScrapClient {
    private static final int MAX_FRAME_LENGTH = 600_000;
    private static final String HEAD_DELIMITER = "</head>";
    private static final String BODY_DELIMITER = "<body";

    private final HttpClient http;

    public NettyScrapClient(HttpClient reactiveClient) {
        this.http = reactiveClient;
    }

    @Override
    public Mono<ScrapResponse> send(ScrapRequest request) {
        HttpClient config = http.headers(headers -> request.headers().map().forEach(headers::add));
        for (HttpCookie cookie : request.cookies()) {
            config.cookie(new DefaultCookie(cookie.getName(), cookie.getValue()));
        }
        AtomicReference<String> lastBuff = new AtomicReference<>("");
        StringBuilder bldr = new StringBuilder(MAX_FRAME_LENGTH * 2);
        AtomicInteger totalSize = new AtomicInteger(0);

        Sinks.One<ScrapResponse> sink = Sinks.one();
        Sinks.Many<ByteBuffer> body = Sinks.many().unicast().onBackpressureBuffer();
        return config.get().uri(request.location())
                .response((res, content) -> {
                    Map<String, List<String>> headers = new HashMap<>();
                    res.responseHeaders().forEach(h ->
                            headers.computeIfAbsent(h.getKey().toLowerCase(), k -> new ArrayList<>()).add(h.getValue()));

                    sink.tryEmitValue(new ScrapResponse(res.status().code(), HttpHeaders.of(headers, (k, v) -> true), body.asFlux()));
                    String contentType = res.responseHeaders().get(HttpHeaderNames.CONTENT_TYPE);
                    if (contentType != null && !contentType.contains(HttpHeaderValues.TEXT_HTML)) {
                        log.debug("[{}] {}", res.requestId(), request.location());
                        log.debug("[{}] {}: {}", res.requestId(), HttpHeaderNames.CONTENT_TYPE, contentType);
                        body.tryEmitComplete();
                        return Flux.empty();
                    }
                    Integer contentLength = res.responseHeaders().getInt(HttpHeaderNames.CONTENT_LENGTH);
                    if (contentLength != null && contentLength > MAX_FRAME_LENGTH * 2) {
                        log.debug("[{}] {}", res.requestId(), request.location());
                        log.debug("[{}] {}: {}", res.requestId(), HttpHeaderNames.CONTENT_LENGTH, contentLength);
                        body.tryEmitComplete();
                        return Mono.empty();
                    }

                    return content.asByteArray();

                }).takeUntil(buff -> {
                    String strBuff = new String(buff).toLowerCase();
                    String twoBuff = bldr.append(lastBuff.get())
                            .append(strBuff)
                            .toString();
                    bldr.setLength(0);
                    lastBuff.set(strBuff);
                    int buffLength = totalSize.getAndAdd(strBuff.length());
                    return buffLength > MAX_FRAME_LENGTH || twoBuff.contains(HEAD_DELIMITER) || twoBuff.contains(BODY_DELIMITER);

                }).map(ByteBuffer::wrap)
                .doOnNext(body::tryEmitNext)
                .doOnComplete(body::tryEmitComplete)
                .then(sink.asMono());
    }
}
