package fr.ght1pc9kc.scraphead.core.scrap;

import fr.ght1pc9kc.scraphead.core.http.WebClient;
import fr.ght1pc9kc.scraphead.core.http.WebRequest;
import fr.ght1pc9kc.scraphead.core.http.WebResponse;
import fr.ght1pc9kc.scraphead.core.model.OGType;
import fr.ght1pc9kc.scraphead.core.model.OpenGraph;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.InputStream;
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
import static org.mockito.Mockito.when;

class OpenGraphScrapperTest {
    private final OpenGraphMetaReader ogReader = spy(new OpenGraphMetaReader());
    private OpenGraphScrapper tested;

    @BeforeEach
    void setUp() {
        WebClient webClient = mock(WebClient.class);
        when(webClient.send(any(WebRequest.class))).thenAnswer(invocation -> {
            WebRequest request = invocation.getArgument(0, WebRequest.class);

            if (!request.location().getPath().endsWith(".html")) {
                Random rd = new Random();
                byte[] arr = new byte[2048];
                rd.nextBytes(arr);
                return Mono.just(
                        new WebResponse(200,
                                HttpHeaders.of(Map.of("content-type", List.of("application/octet-stream")), (l, r) -> true),
                                Flux.just(ByteBuffer.wrap(arr)))
                );
            }

            ByteBuffer byteBuffer;
            try (InputStream is = OpenGraphScrapperTest.class.getResourceAsStream(request.location().getPath().replaceAll("^/", ""))) {
                if (is == null) {
                    return Mono.just(new WebResponse(404, null, Flux.empty()));
                }

                byteBuffer = ByteBuffer.wrap(is.readAllBytes());
            }
            return Mono.just(new WebResponse(200,
                    HttpHeaders.of(Map.of("content-type", List.of("text/html")), (l, r) -> true), Flux.just(byteBuffer)));
        });
        reset(ogReader);
        tested = new OpenGraphScrapper(webClient, ogReader, List.of());
    }

    @Test
    void should_parse_opengraph() throws MalformedURLException {
        URI page = URI.create("https://blog.ght1pc9kc.fr/og-head-test.html");
        OpenGraph actual = tested.scrap(page).block();

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
    void should_parse_opengraph_with_empty_fields() throws MalformedURLException {
        URI page = URI.create("https://blog.ght1pc9kc.fr/ght-bad-parsing.html");
        OpenGraph actual = tested.scrap(page).block();

        Assertions.assertThat(actual).isEqualTo(OpenGraph.builder()
                .title("Les Critères de recherche avec Juery")
                .type(OGType.ARTICLE)
                .url(new URL("https://blog.ght1pc9kc.fr/2021/les-crit%C3%A8res-de-recherche-avec-juery.html"))
                .build());
    }

    @Test
    void should_parse_opengraph_with_apostrophe() {
        URI page = URI.create("https://blog.ght1pc9kc.fr/apostrophe.html");
        OpenGraph actual = tested.scrap(page).block();

        Assertions.assertThat(actual).isNotNull();
        assertAll(
                () -> Assertions.assertThat(actual.title).isEqualTo("Économiseur d'écran personnalisé avec XSecureLock"),
                () -> Assertions.assertThat(actual.type).isEqualTo(OGType.ARTICLE),
                () -> Assertions.assertThat(actual.image).isEqualTo(URI.create("https://d1g3mdmxf8zbo9.cloudfront.net/images/i3/xsecurelock@2x.jpg")),
                () -> Assertions.assertThat(actual.locale).isEqualTo(Locale.FRANCE)
        );
    }

    @Test
    void should_parse_non_utf8_file() {
        URI page = URI.create("https://blog.ght1pc9kc.fr/dev-empty-metas-error.html");
        OpenGraph actual = tested.scrap(page).block();

        Assertions.assertThat(actual).isNotNull();
        assertAll(
                () -> Assertions.assertThat(actual.title).isEqualTo("Panasonic proposera une semaine de travail de " +
                        "quatre jours à ses employés au Japon, dans le but d'améliorer la productivité et d'attirer " +
                        "les meilleurs talents"),
                () -> Assertions.assertThat(actual.type).isEqualTo(OGType.ARTICLE),
                () -> Assertions.assertThat(actual.image).isEqualTo(URI.create("https://www.developpez.com/images/logos/emploi.png")),
                () -> Assertions.assertThat(actual.locale).isEqualTo(Locale.FRANCE)
        );
    }

    @ParameterizedTest
    @CsvSource({
            "https://blog.ght1pc9kc.fr/no-encoding-file.html",
            "https://blog.ght1pc9kc.fr/not-found.html",
            "https://blog.ght1pc9kc.fr/podcast.mp3"
    })
    void should_parse_no_encoding_file(String url) {
        URI page = URI.create(url);
        StepVerifier.create(tested.scrap(page)).verifyComplete();
    }
}