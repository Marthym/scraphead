package fr.ght1pc9kc.scraphead.core;

import fr.ght1pc9kc.scraphead.core.http.WebClient;
import fr.ght1pc9kc.scraphead.core.scrap.OpenGraphMetaReader;
import fr.ght1pc9kc.scraphead.core.scrap.OpenGraphScrapper;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class HeadScrapers {
    public static HeadScraperBuilder builder(WebClient webClient) {
        return new HeadScraperBuilder(webClient, new OpenGraphMetaReader());
    }

    @RequiredArgsConstructor
    public static class HeadScraperBuilder {
        private final WebClient webClient;
        private final OpenGraphMetaReader ogReader;
        private final List<OpenGraphPlugin> scrapperPlugins = new ArrayList<>();

        public HeadScraperBuilder registerPlugin(OpenGraphPlugin plugin) {
            scrapperPlugins.add(plugin);
            return this;
        }

        public HeadScrapper build() {
            return new OpenGraphScrapper(webClient, ogReader, scrapperPlugins);
        }
    }
}
