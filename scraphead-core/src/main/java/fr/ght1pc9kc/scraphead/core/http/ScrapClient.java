package fr.ght1pc9kc.scraphead.core.http;

import reactor.core.publisher.Mono;

public interface ScrapClient {
    Mono<ScrapResponse> send(ScrapRequest request);
}
