package fr.ght1pc9kc.scraphead.core;

import fr.ght1pc9kc.scraphead.core.http.WebClient;
import fr.ght1pc9kc.scraphead.core.scrap.OpenGraphMetaReader;
import fr.ght1pc9kc.scraphead.core.scrap.HeadScrapperImpl;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder for {@link HeadScrapper}
 */
@UtilityClass
public class HeadScrapers {
    public static HeadScraperBuilder builder(WebClient webClient) {
        return new HeadScraperBuilder(webClient, new OpenGraphMetaReader());
    }

    @RequiredArgsConstructor
    public static class HeadScraperBuilder {
        private final WebClient webClient;
        private final OpenGraphMetaReader ogReader;
        private final List<ScraperPlugin> scrapperPlugins = new ArrayList<>();

        /**
         * Register a {@link ScraperPlugin} to the {@link HeadScrapper}
         *
         * @param plugin The plugin to register
         * @return  The builder
         */
        public HeadScraperBuilder registerPlugin(ScraperPlugin plugin) {
            scrapperPlugins.add(plugin);
            return this;
        }

        public HeadScrapper build() {
            return new HeadScrapperImpl(webClient, ogReader, scrapperPlugins);
        }
    }
}
