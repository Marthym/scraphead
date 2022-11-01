package fr.ght1pc9kc.scraphead.core.http;

import lombok.With;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reactor.core.publisher.Flux;

import java.net.http.HttpHeaders;
import java.nio.ByteBuffer;

public record ScrapResponse(
        int status,
        @Nullable HttpHeaders headers,
        @NotNull @With Flux<ByteBuffer> body
) {
}
