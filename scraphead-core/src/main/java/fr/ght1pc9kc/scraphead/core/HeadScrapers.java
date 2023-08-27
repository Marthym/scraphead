package fr.ght1pc9kc.scraphead.core;

import fr.ght1pc9kc.scraphead.core.http.ScrapClient;
import fr.ght1pc9kc.scraphead.core.model.Header;
import fr.ght1pc9kc.scraphead.core.scrap.DocumentMetaReader;
import fr.ght1pc9kc.scraphead.core.scrap.HeadScraperImpl;
import fr.ght1pc9kc.scraphead.core.scrap.collectors.HtmlHeadCollector;
import fr.ght1pc9kc.scraphead.core.scrap.collectors.LinksCollector;
import fr.ght1pc9kc.scraphead.core.scrap.collectors.MetaDataCollector;
import fr.ght1pc9kc.scraphead.core.scrap.collectors.OpenGraphCollector;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder for {@link HeadScraper}
 *
 * <p>With all collectors :</p>
 * <pre>{@code
 *  ScrapClient scrapClient = new NettyScrapClient();
 *  return HeadScrapers.builder(scrapClient)
 *      .useMetaTitleAndDescr()
 *      .useOpengraph()
 *      .build();
 * }</pre>
 *
 * <p>With limited collectors usage :</p>
 * <pre>{@code
 *  ScrapClient scrapClient = new NettyScrapClient();
 *  return HeadScrapers.builder(scrapClient)
 *      .useMetaTitleAndDescr()
 *      .useOpengraph()
 *      .build();
 * }</pre>
 */
@UtilityClass
public class HeadScrapers {
    public static HeadScraperBuilder builder(ScrapClient webClient) {
        return new HeadScraperBuilder(webClient);
    }

    @RequiredArgsConstructor
    public static class HeadScraperBuilder {
        private final ScrapClient webClient;
        private final List<MetaDataCollector<? extends Header>> collectors = new ArrayList<>(3);

        public HeadScraper build() {
            if (collectors.isEmpty()) {
                useMetaTitleAndDescr();
                useOpengraph();
                useLinks();
            }
            return new HeadScraperImpl(webClient, new DocumentMetaReader(List.copyOf(collectors)));
        }

        public HeadScraperBuilder useMetaTitleAndDescr() {
            collectors.add(new HtmlHeadCollector());
            return this;
        }

        public HeadScraperBuilder useOpengraph() {
            collectors.add(new OpenGraphCollector());
            return this;
        }

        public HeadScraperBuilder useLinks() {
            collectors.add(new LinksCollector());
            return this;
        }
    }
}
