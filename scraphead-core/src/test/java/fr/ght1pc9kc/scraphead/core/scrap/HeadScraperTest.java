package fr.ght1pc9kc.scraphead.core.scrap;

import fr.ght1pc9kc.scraphead.core.HeadScraper;
import fr.ght1pc9kc.scraphead.core.http.ScrapClient;
import fr.ght1pc9kc.scraphead.core.http.ScrapRequest;
import fr.ght1pc9kc.scraphead.core.http.ScrapResponse;
import fr.ght1pc9kc.scraphead.core.model.Metas;
import fr.ght1pc9kc.scraphead.core.model.ex.HeadScrapingException;
import fr.ght1pc9kc.scraphead.core.model.ex.NetworkScrapException;
import fr.ght1pc9kc.scraphead.core.model.opengraph.OGType;
import fr.ght1pc9kc.scraphead.core.model.opengraph.OpenGraph;
import fr.ght1pc9kc.scraphead.core.scrap.collectors.HtmlHeadCollector;
import fr.ght1pc9kc.scraphead.core.scrap.collectors.OpenGraphCollector;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.InputStream;
import java.net.HttpCookie;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpHeaders;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HeadScraperTest {
    private final DocumentMetaReader ogReader = spy(new DocumentMetaReader(List.of(
            new HtmlHeadCollector(),
            new OpenGraphCollector()
    )));
    private HeadScraperImpl tested;

    private final ScrapClient webClient = mock(ScrapClient.class);

    @BeforeEach
    void setUp() {
        when(webClient.send(any(ScrapRequest.class))).thenAnswer(invocation -> {
            ScrapRequest request = invocation.getArgument(0, ScrapRequest.class);

            if (!request.location().getPath().endsWith(".html")) {
                Random rd = new Random();
                byte[] arr = new byte[2048];
                rd.nextBytes(arr);
                return Mono.just(
                        new ScrapResponse(200, request.location(),
                                HttpHeaders.of(Map.of("content-type", List.of("application/octet-stream")), (l, r) -> true),
                                Flux.just(ByteBuffer.wrap(arr)))
                );
            }

            ByteBuffer byteBuffer;
            try (InputStream is = HeadScraperTest.class.getResourceAsStream(request.location().getPath().replaceAll("^/", ""))) {
                if (is == null) {
                    return Mono.just(new ScrapResponse(404, request.location(), null, Flux.empty()));
                }

                byteBuffer = ByteBuffer.wrap(is.readAllBytes());
            }
            return Mono.just(new ScrapResponse(200, request.location(),
                    HttpHeaders.of(Map.of("content-type", List.of("text/html")), (l, r) -> true), Flux.just(byteBuffer)));
        });
        reset(ogReader);
        tested = new HeadScraperImpl(webClient, ogReader);
    }

    @Test
    void should_parse_opengraph() throws MalformedURLException {
        URI page = URI.create("https://blog.ght1pc9kc.fr/og-head-test.html");
        OpenGraph actual = tested.scrap(page).map(Metas::og).block();

        Assertions.assertThat(actual).isEqualTo(OpenGraph.builder()
                .title("De Paris à Toulouse")
                .description("Déplacement des serveurs de l’infrastructure i-Run depuis Paris jusqu’à Toulouse chez " +
                        "notre hébergeur FullSave. Nouvelles machines, nouvelle infra pour plus de résilience et une " +
                        "meilleure tenue de la charge sur les sites publics comme sur le backoffice.")
                .type(OGType.ARTICLE)
                .url(new URL("https://blog.i-run.si/posts/silife/infra-de-paris-a-toulouse/"))
                .image(URI.create("https://blog.i-run.si/posts/silife/infra-de-paris-a-toulouse/featured.jpg"))
                .build());
    }

    @Test
    void should_parse_title_and_descr() {
        URI page = URI.create("https://blog.ght1pc9kc.fr/og-head-test.html");
        StepVerifier.create(tested.scrap(page))
                .assertNext(actual -> assertAll(
                        () -> Assertions.assertThat(actual.title()).isEqualTo("De Paris à Toulouse • wi-run, the code"),
                        () -> Assertions.assertThat(actual.description()).isEqualTo("Déplacement des serveurs de " +
                                "l’infrastructure i-Run depuis Paris jusqu’à Toulouse chez notre hébergeur FullSave. " +
                                "Nouvelles machines, nouvelle infra pour plus de résilience et une meilleure tenue de " +
                                "la charge sur les sites publics comme sur le backoffice.")
                )).verifyComplete();
    }

    @Test
    void should_parse_opengraph_with_empty_fields() throws MalformedURLException {
        URI page = URI.create("https://blog.ght1pc9kc.fr/ght-bad-parsing.html");
        OpenGraph actual = tested.scrap(page).map(Metas::og).block();

        Assertions.assertThat(actual).isEqualTo(OpenGraph.builder()
                .title("Les Critères de recherche avec Juery")
                .type(OGType.ARTICLE)
                .url(new URL("https://blog.ght1pc9kc.fr/2021/les-crit%C3%A8res-de-recherche-avec-juery.html"))
                .description("")
                .build());
    }

    @Test
    void should_parse_opengraph_with_apostrophe() {
        URI page = URI.create("https://blog.ght1pc9kc.fr/apostrophe.html");
        OpenGraph actual = tested.scrap(page).map(Metas::og).block();

        Assertions.assertThat(actual).isNotNull();
        assertAll(
                () -> Assertions.assertThat(actual.title()).isEqualTo("Économiseur d'écran personnalisé avec XSecureLock"),
                () -> Assertions.assertThat(actual.type()).isEqualTo(OGType.ARTICLE),
                () -> Assertions.assertThat(actual.image()).isEqualTo(URI.create("https://d1g3mdmxf8zbo9.cloudfront.net/images/i3/xsecurelock@2x.jpg")),
                () -> Assertions.assertThat(actual.locale()).isEqualTo(Locale.FRANCE)
        );
    }

    @Test
    void should_parse_non_utf8_file() {
        URI page = URI.create("https://blog.ght1pc9kc.fr/dev-empty-metas-error.html");
        OpenGraph actual = tested.scrap(page).map(Metas::og).block();

        Assertions.assertThat(actual).isNotNull();
        assertAll(
                () -> Assertions.assertThat(actual.title()).isEqualTo("Panasonic proposera une semaine de travail de " +
                        "quatre jours à ses employés au Japon, dans le but d'améliorer la productivité et d'attirer " +
                        "les meilleurs talents"),
                () -> Assertions.assertThat(actual.type()).isEqualTo(OGType.ARTICLE),
                () -> Assertions.assertThat(actual.image()).isEqualTo(URI.create("https://www.developpez.com/images/logos/emploi.png")),
                () -> Assertions.assertThat(actual.locale()).isEqualTo(Locale.FRANCE)
        );
    }

    @Test
    void should_parse_with_title_out_of_head() {
        URI page = URI.create("https://blog.ght1pc9kc.fr/title-out-of-head.html");
        String actual = tested.scrap(page).map(Metas::title).block();

        Assertions.assertThat(actual)
                .isNotNull()
                .isEqualTo("Observability with Spring Boot 3");
    }

    @ParameterizedTest
    @CsvSource({
            "https://blog.ght1pc9kc.fr/no-encoding-file.html",
            "https://blog.ght1pc9kc.fr/not-found.html",
            "https://blog.ght1pc9kc.fr/podcast.mp3"
    })
    void should_parse_no_encoding_file(String url) {
        URI page = URI.create(url);
        StepVerifier.create(tested.scrap(page).map(Metas::og))
                .expectNextMatches(OpenGraph::isEmpty)
                .verifyComplete();
    }

    @Test
    void should_avoid_crash_when_error() {
        reset(webClient);
        when(webClient.send(any(ScrapRequest.class))).thenThrow(new IllegalArgumentException());
        URI page = URI.create("/relative/path");
        StepVerifier.create(tested.scrap(page).map(Metas::og))
                .verifyError(HeadScrapingException.class);
    }

    @Test
    void should_avoid_crash_when_error_on_flux() {
        reset(webClient);
        when(webClient.send(any(ScrapRequest.class))).thenReturn(Mono.error(new IllegalArgumentException()));
        URI page = URI.create("/relative/path");
        StepVerifier.create(tested.scrap(page).map(Metas::og))
                .verifyError(NetworkScrapException.class);
    }

    @Test
    void should_use_plugin() {
        URI page = URI.create("https://blog.ght1pc9kc.fr/og-head-test.html");
        HeadScraper pluginTested = new HeadScraperImpl(webClient, ogReader);
        StepVerifier.create(pluginTested.scrap(ScrapRequest.builder(page)
                        .addHeader("X-Dummy", "test")
                        .addCookie("COOKIE_TEST", "test")
                        .build()))
                .expectNextCount(1)
                .verifyComplete();

        ArgumentCaptor<ScrapRequest> captor = ArgumentCaptor.forClass(ScrapRequest.class);
        verify(webClient).send(captor.capture());

        ScrapRequest actual = captor.getValue();
        assertAll(
                () -> Assertions.assertThat(actual.headers()).isEqualTo(HttpHeaders.of(Map.of(
                        "Accept-Charset", List.of("UTF-8"),
                        "X-Dummy", List.of("test")
                ), (l, r) -> true)),
                () -> Assertions.assertThat(actual.cookies()).isEqualTo(List.of(
                        new HttpCookie("COOKIE_TEST", "test")
                ))
        );
    }
}