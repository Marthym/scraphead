package fr.ght1pc9kc.scraphead.core.scrap;

import fr.ght1pc9kc.scraphead.core.scrap.model.Node;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

class HtmlFragmentParserTest {
    @ParameterizedTest
    @MethodSource("parametersForShould_extract_meta_headers")
    void should_extract_meta_headers(String htmlFile, List<Node> expected) throws IOException {
        InputStream is = OpenGraphScrapperTest.class.getResourceAsStream(htmlFile);
        Assertions.assertThat(is).isNotNull();
        List<Node> actuals = HtmlFragmentParser.parse(new String(is.readAllBytes()));
        Assertions.assertThat(actuals)
                .isNotNull()
                .containsAll(expected);
    }

    private static Stream<Arguments> parametersForShould_extract_meta_headers() {
        return Stream.of(
                Arguments.of("apostrophe.html", List.of(
                        new Node("meta", Map.of("property", "og:title",
                                "content", "Économiseur d'écran personnalisé avec XSecureLock")),
                        new Node("meta", Map.of("property", "og:type", "content", "article")),
                        new Node("meta", Map.of("property", "og:image",
                                "content", "https://d1g3mdmxf8zbo9.cloudfront.net/images/i3/xsecurelock@2x.jpg")),
                        new Node("meta", Map.of("property", "og:locale", "content", "fr_FR")),
                        new Node("meta", Map.of("property", "og:description", "name", "description",
                                "content", "XSecureLock permet de verrouiller une session " +
                                        "X11 et délègue la partie économiseur d'écran à un programme tiers, permettant " +
                                        "de personnaliser..."))
                )),
                Arguments.of("ght-bad-parsing.html", List.of(
                        new Node("meta", Map.of("property", "og:title",
                                "content", "Les Critères de recherche avec Juery")),
                        new Node("meta", Map.of("property", "og:type", "content", "article")),
                        new Node("meta", Map.of("property", "og:url",
                                "content", "https://blog.ght1pc9kc.fr/2021/les-crit%C3%A8res-de-recherche-avec-juery.html")),
                        new Node("meta", Map.of("property", "og:description", "content", ""))
                )),
                Arguments.of("og-head-test.html", List.of(
                        new Node("meta", Map.of("property", "og:title", "content", "De Paris à Toulouse")),
                        new Node("meta", Map.of("property", "og:type", "content", "article")),
                        new Node("meta", Map.of("property", "og:url",
                                "content", "https://blog.i-run.si/posts/silife/infra-de-paris-a-toulouse/")),
                        new Node("meta", Map.of("property", "og:image",
                                "content", "https://blog.i-run.si/posts/silife/infra-de-paris-a-toulouse/featured.jpg")),
                        new Node("meta", Map.of("property", "og:description",
                                "content", "Déplacement des serveurs de l’infrastructure " +
                                        "i-Run depuis Paris jusqu’à Toulouse chez notre hébergeur FullSave. Nouvelles machines, " +
                                        "nouvelle infra pour plus de résilience et une meilleure tenue de la charge sur les " +
                                        "sites publics comme sur le backoffice.")),
                        new Node("meta", Map.of("property", "test:double:quote", "content", "With ",
                                "double", "", "quote\"", ""))
                )),
                Arguments.of("youtube.html", List.of(
                        new Node("meta", Map.of("property", "og:title",
                                "content", "Programming / Coding / Hacking music vol.16 (CONNECTION LOST)")),
                        new Node("meta", Map.of("property", "og:type", "content", "video.other")),
                        new Node("meta", Map.of("property", "og:url",
                                "content", "https://www.youtube.com/watch?v=l9nh1l8ZIJQ")),
                        new Node("meta", Map.of("property", "og:image",
                                "content", "https://i.ytimg.com/vi/l9nh1l8ZIJQ/maxresdefault.jpg")),
                        new Node("meta", Map.of("property", "og:site_name", "content", "YouTube")),
                        new Node("meta", Map.of("property", "og:description",
                                "content", "Stay with Jim ^-^ Enjoy and do not forget to say " +
                                        "thank you!Support on Patreon will motivate me more. I need to know that you guys " +
                                        "need this stuff and you ape..."))))
        );
    }
}