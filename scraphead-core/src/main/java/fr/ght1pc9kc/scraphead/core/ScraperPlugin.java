package fr.ght1pc9kc.scraphead.core;

import fr.ght1pc9kc.scraphead.core.opengraph.OpenGraph;
import reactor.core.publisher.Mono;

import java.net.HttpCookie;
import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * Interface to implement in order to add a OG scrapper plugin.
 * <p>
 * A OG Scrapper Plugin allow to include additional Cookie or Headers into scrapping http request before send it.
 */
public interface ScraperPlugin {
    default String name() {
        return this.getClass().getSimpleName() + " OpenGraph scrapper plugin";
    }

    /**
     * The let the scrapper to know if the plugin must be used for the current request
     *
     * @param location The URI of the request
     * @return true is the scrapper must use the plugin for the request
     */
    boolean isApplicable(URI location);

    /**
     * If the scrapper must use the plugin give the Cookie to include to the request
     *
     * @return A map of Cookie Name / Cookie Value
     */
    default List<HttpCookie> additionalCookies() {
        return List.of();
    }

    /**
     * If the scrapper must use the plugin give the Headers to include to the request
     *
     * @return A map of Header Name / Header Value
     */
    default Map<String, String> additionalHeaders() {
        return Map.of();
    }

    default Mono<OpenGraph> postTreatment(OpenGraph openGraph) {
        return Mono.just(openGraph);
    }
}
