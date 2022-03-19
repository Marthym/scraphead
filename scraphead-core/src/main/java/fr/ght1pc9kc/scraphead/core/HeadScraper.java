package fr.ght1pc9kc.scraphead.core;

import fr.ght1pc9kc.scraphead.core.model.Metas;
import fr.ght1pc9kc.scraphead.core.model.links.Links;
import fr.ght1pc9kc.scraphead.core.model.opengraph.OpenGraph;
import fr.ght1pc9kc.scraphead.core.model.twitter.TwitterCard;
import reactor.core.publisher.Mono;

import java.net.URI;

public interface HeadScraper {
    Mono<Metas> scrap(URI location);

    Mono<OpenGraph> scrapOpenGraph(URI location);

    Mono<TwitterCard> scrapTwitterCard(URI location);

    Mono<Links> scrapLinks(URI location);
}
