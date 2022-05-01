package fr.ght1pc9kc.scraphead.core;

import fr.ght1pc9kc.scraphead.core.http.WebRequest;
import fr.ght1pc9kc.scraphead.core.model.Metas;
import reactor.core.publisher.Mono;

import java.net.URI;

public interface HeadScraper {
    Mono<Metas> scrap(URI location);

    Mono<Metas> scrap(WebRequest request);
}
