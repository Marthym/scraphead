package fr.ght1pc9kc.scraphead.netty.http;


import fr.ght1pc9kc.scraphead.core.http.ScrapClient;
import fr.ght1pc9kc.scraphead.core.http.ScrapRequest;
import fr.ght1pc9kc.scraphead.core.http.ScrapResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.net.HttpCookie;
import java.net.http.HttpHeaders;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private static final ByteBuf FRAME_HEAD_DELIMITER = Unpooled.wrappedBuffer("</head>".getBytes(StandardCharsets.UTF_8));
    private static final ByteBuf FRAME_BODY_DELIMITER = Unpooled.wrappedBuffer("<body".getBytes(StandardCharsets.UTF_8));

    private final HttpClient http;

    public NettyScrapClient(HttpClient reactiveClient) {
        this.http = reactiveClient.doOnConnected(c ->
                c.addHandler(new DelimiterBasedFrameDecoder(MAX_FRAME_LENGTH, FRAME_HEAD_DELIMITER, FRAME_BODY_DELIMITER)));//FIXME: when reactor-netty {@link HttpOperations} will implement addHandlerLast
    }

    @Override
    public Mono<ScrapResponse> send(ScrapRequest request) {
        HttpClient config = http.headers(headers -> request.headers().map().forEach(headers::add));
        for (HttpCookie cookie : request.cookies()) {
            config.cookie(new DefaultCookie(cookie.getName(), cookie.getValue()));
        }
        return config.get().uri(request.location())
                .response((res, content) -> {
                    String contentType = res.responseHeaders().get(HttpHeaderNames.CONTENT_TYPE);
                    if (contentType != null && !contentType.contains(HttpHeaderValues.TEXT_HTML)) {
                        log.debug("[{}] {}", res.requestId(), request.location());
                        log.debug("[{}] {}: {}", res.requestId(), HttpHeaderNames.CONTENT_TYPE, contentType);
                        return Flux.empty();
                    }
                    Integer contentLength = res.responseHeaders().getInt(HttpHeaderNames.CONTENT_LENGTH);
                    if (contentLength != null && contentLength > MAX_FRAME_LENGTH * 2) {
                        log.debug("[{}] {}", res.requestId(), request.location());
                        log.debug("[{}] {}: {}", res.requestId(), HttpHeaderNames.CONTENT_LENGTH, contentLength);
                        return Flux.empty();
                    }

                    return content.asByteArray().map(buff -> {
                        Map<String, List<String>> headers = new HashMap<>();
                        res.responseHeaders().forEach(h ->
                                headers.computeIfAbsent(h.getKey().toLowerCase(), k -> new ArrayList<>()).add(h.getValue()));
                        return new ScrapResponse(res.status().code(), HttpHeaders.of(headers, (k, v) -> true), Flux.just(ByteBuffer.wrap(buff)));
                    });
                }).next();
    }
}
