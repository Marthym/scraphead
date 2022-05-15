package fr.ght1pc9kc.scraphead.core;

import fr.ght1pc9kc.scraphead.core.http.ScrapClient;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;

class HeadScrapersTest {
    @Test
    void should_build_head_scraper() {
        ScrapClient scrapClient = mock(ScrapClient.class);
        HeadScraper actual = HeadScrapers.builder(scrapClient).build();

        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void should_build_head_scraper_with_use() {
        ScrapClient scrapClient = mock(ScrapClient.class);
        HeadScraper actual = HeadScrapers.builder(scrapClient)
                .useMetaTitleAndDescr()
                .useOpengraph()
                .useLinks()
                .build();

        Assertions.assertThat(actual).isNotNull();
    }
}