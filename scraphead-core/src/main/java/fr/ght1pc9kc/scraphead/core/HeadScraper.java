package fr.ght1pc9kc.scraphead.core;

import fr.ght1pc9kc.scraphead.core.http.ScrapRequest;
import fr.ght1pc9kc.scraphead.core.model.Metas;
import fr.ght1pc9kc.scraphead.core.scrap.collectors.MetaDataCollector;
import reactor.core.publisher.Mono;

import java.net.URI;

/**
 * Allow to scrap header from {@link URI} or request
 */
public interface HeadScraper {
    /**
     * Download the {@code <head/>} tag of the given {@link URI}
     *
     * @param location The location of the page
     * @return The retrieving metas data depending on the {@link MetaDataCollector} used
     */
    Mono<Metas> scrap(URI location);

    /**
     * Download the {@code <head/>} tag of the given {@link URI}
     *
     * @param request The request to do for accessing the page. {@link ScrapRequest} allow to give {@link java.net.HttpCookie}
     *                or {@link java.net.http.HttpHeaders} to the request.
     * @return The retrieving metas data depending on the {@link MetaDataCollector} used
     */
    Mono<Metas> scrap(ScrapRequest request);
}
