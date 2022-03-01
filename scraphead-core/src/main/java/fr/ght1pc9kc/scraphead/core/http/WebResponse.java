package fr.ght1pc9kc.scraphead.core.http;

import reactor.core.publisher.Flux;

import java.net.http.HttpHeaders;
import java.nio.ByteBuffer;

public record WebResponse(
        int status,
        HttpHeaders headers,
        Flux<ByteBuffer> body
) {
}
