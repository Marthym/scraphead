package fr.ght1pc9kc.scraphead.core;

import fr.ght1pc9kc.scraphead.core.model.OpenGraph;
import reactor.core.publisher.Mono;

import java.net.URI;

public interface HeadScrapper {
    Mono<OpenGraph> scrap(URI location);
}
