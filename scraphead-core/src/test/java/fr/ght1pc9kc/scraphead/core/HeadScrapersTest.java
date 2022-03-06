package fr.ght1pc9kc.scraphead.core;

import fr.ght1pc9kc.scraphead.core.http.WebClient;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;

class HeadScrapersTest {
    @Test
    void should_build_head_scraper() {
        WebClient webClient = mock(WebClient.class);
        ScraperPlugin scraperPlugin = mock(ScraperPlugin.class);
        HeadScrapper actual = HeadScrapers.builder(webClient)
                .registerPlugin(scraperPlugin)
                .build();

        Assertions.assertThat(actual).isNotNull();
    }
}