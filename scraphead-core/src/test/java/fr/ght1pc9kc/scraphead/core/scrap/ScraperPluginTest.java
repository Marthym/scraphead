package fr.ght1pc9kc.scraphead.core.scrap;

import fr.ght1pc9kc.scraphead.core.ScraperPlugin;
import fr.ght1pc9kc.scraphead.core.http.WebClient;
import fr.ght1pc9kc.scraphead.core.http.WebResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.net.http.HttpHeaders;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ScraperPluginTest {

    private final ScraperPlugin plugin = mock(ScraperPlugin.class);

    private OpenGraphScrapper tested;

    @BeforeEach
    void setUp() {
        when(plugin.isApplicable(any())).thenReturn(true);
        WebClient webClient = mock(WebClient.class);
        when(webClient.send(any())).thenReturn(Mono.just(
                new WebResponse(200,
                        HttpHeaders.of(Map.of("content-type", List.of("application/json")), (l, r) -> true),
                        Flux.empty()))
        );
        this.tested = new OpenGraphScrapper(webClient, new OpenGraphMetaReader(), List.of(plugin));
    }

    @Test
    void should_use_plugin_for_scrapper() {
        StepVerifier.create(
                tested.scrap(URI.create("https://www.youtube.com/watch?v=l9nh1l8ZIJQ"))
        ).verifyComplete();

        verify(plugin, times(1)).additionalHeaders();
        verify(plugin, times(1)).additionalCookies();
    }
}
