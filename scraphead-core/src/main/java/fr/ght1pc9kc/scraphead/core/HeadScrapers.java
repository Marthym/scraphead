package fr.ght1pc9kc.scraphead.core;

import fr.ght1pc9kc.scraphead.core.http.WebClient;
import fr.ght1pc9kc.scraphead.core.scrap.DocumentMetaReader;
import fr.ght1pc9kc.scraphead.core.scrap.HeadScraperImpl;
import fr.ght1pc9kc.scraphead.core.scrap.MetaDataCollector;
import fr.ght1pc9kc.scraphead.core.scrap.collectors.LinksCollector;
import fr.ght1pc9kc.scraphead.core.scrap.collectors.OpenGraphCollector;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;

import java.util.List;

/**
 * Builder for {@link HeadScraper}
 */
@UtilityClass
public class HeadScrapers {
    public static HeadScraperBuilder builder(WebClient webClient) {
        return new HeadScraperBuilder(webClient);
    }

    @RequiredArgsConstructor
    public static class HeadScraperBuilder {
        private final WebClient webClient;

        public HeadScraper build() {
            List<MetaDataCollector> metaDataCollectors = List.of(
                    new OpenGraphCollector(),
                    new LinksCollector()
            );
            return new HeadScraperImpl(webClient, new DocumentMetaReader(metaDataCollectors));
        }
    }
}
