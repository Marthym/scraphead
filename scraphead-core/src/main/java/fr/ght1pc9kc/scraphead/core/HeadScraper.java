package fr.ght1pc9kc.scraphead.core;

import fr.ght1pc9kc.scraphead.core.model.Metas;
import reactor.core.publisher.Mono;

import java.net.HttpCookie;
import java.net.URI;
import java.net.http.HttpHeaders;
import java.util.List;

public interface HeadScraper {
    Mono<Metas> scrap(URI location);

    Mono<Metas> scrap(URI location, HttpHeaders headers, List<HttpCookie> cookies);
}
