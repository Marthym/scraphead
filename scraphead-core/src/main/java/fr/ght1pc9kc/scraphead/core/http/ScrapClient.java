package fr.ght1pc9kc.scraphead.core.http;

import reactor.core.publisher.Mono;

/**
 * Wrapper for HTTP client
 */
public interface ScrapClient {
    /**
     * Send the HTTP request from request informations
     *
     * @param request The request containing HTTP data to locate and retrieve the headers
     * @return The {@link ScrapResponse} containing {@link reactor.core.publisher.Flux} of {@link java.nio.ByteBuffer}
     * representing the html {@code <head/>}
     */
    Mono<ScrapResponse> send(ScrapRequest request);
}
