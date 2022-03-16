package fr.ght1pc9kc.scraphead.core.scrap;

import fr.ght1pc9kc.scraphead.core.model.Metas;
import fr.ght1pc9kc.scraphead.core.model.opengraph.OGType;
import fr.ght1pc9kc.scraphead.core.model.opengraph.OpenGraph;
import fr.ght1pc9kc.scraphead.core.scrap.collectors.OpenGraphCollector;
import org.assertj.core.api.Assertions;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Locale;

import static fr.ght1pc9kc.scraphead.core.scrap.OGScrapperUtils.META_CONTENT;
import static fr.ght1pc9kc.scraphead.core.scrap.OGScrapperUtils.META_NAME;
import static fr.ght1pc9kc.scraphead.core.scrap.OGScrapperUtils.META_PROPERTY;

class DocumentMetaReaderTest {
    private final DocumentMetaReader tested = new DocumentMetaReader(List.of(new OpenGraphCollector()));

    @Test
    void should_build_from_metas() throws MalformedURLException {
        Document document = new Document("");
        document.appendChildren(List.of(
                new Element("meta")
                        .attr(META_NAME, "twitter:title").attr(META_CONTENT, "Title de twitter"),
                new Element("meta")
                        .attr(META_PROPERTY, "og:title").attr(META_CONTENT, "Title de OG"),
                new Element("meta")
                        .attr(META_PROPERTY, "og:image").attr(META_CONTENT, "https://blog.ght1pc9kc.fr/img/featured.jpg"),
                new Element("meta")
                        .attr(META_PROPERTY, "og:type").attr(META_CONTENT, "article"),
                new Element("meta")
                        .attr(META_PROPERTY, "og:url").attr(META_CONTENT, "https://blog.ght1pc9kc.fr/img/over-the-top.html"),
                new Element("meta")
                        .attr(META_PROPERTY, "og:description").attr(META_CONTENT, "Description de OG"),
                new Element("meta")
                        .attr(META_PROPERTY, "og:locale").attr(META_CONTENT, "fr-fr")
        ));
        Metas actual = tested.read(document);

        Assertions.assertThat(actual.og()).isEqualTo(
                OpenGraph.builder()
                        .title("Title de OG")
                        .image(URI.create("https://blog.ght1pc9kc.fr/img/featured.jpg"))
                        .type(OGType.ARTICLE)
                        .url(new URL("https://blog.ght1pc9kc.fr/img/over-the-top.html"))
                        .description("Description de OG")
                        .locale(Locale.FRANCE)
                        .build());
    }

    @Test
    void should_build_from_metas_with_empty_fields() throws MalformedURLException {
        Document document = new Document("");
        document.appendChildren(List.of(
                new Element("meta")
                        .attr(META_NAME, "twitter:title").attr(META_CONTENT, "Title de twitter"),
                new Element("meta")
                        .attr(META_PROPERTY, "og:title").attr(META_CONTENT, "Title de OG"),
                new Element("meta")
                        .attr(META_PROPERTY, "og:image").attr(META_CONTENT, ""),
                new Element("meta")
                        .attr(META_PROPERTY, "og:type").attr(META_CONTENT, "article"),
                new Element("meta")
                        .attr(META_PROPERTY, "og:url").attr(META_CONTENT, "https://blog.ght1pc9kc.fr/img/over-the-top.html"),
                new Element("meta")
                        .attr(META_PROPERTY, "og:locale").attr(META_CONTENT, "fr-fr")
        ));
        Metas actual = tested.read(document);

        Assertions.assertThat(actual.og()).isEqualTo(
                OpenGraph.builder()
                        .title("Title de OG")
                        .type(OGType.ARTICLE)
                        .url(new URL("https://blog.ght1pc9kc.fr/img/over-the-top.html"))
                        .locale(Locale.FRANCE)
                        .build());
    }

    @ParameterizedTest
    @CsvSource({
            "http://obiwan.kenobi.jedi/tatooine/featured.jpg",
            "//obiwan.kenobi.jedi/tatooine/featured.jpg",
            "/tatooine/featured.jpg",
            "../tatooine/featured.jpg",
    })
    void should_parse_relative_url(String imageLink) {
        String baseUri = "http://obiwan.kenobi.jedi/posts/padawan-to-master";
        Document document = new Document(baseUri);
        document.appendChildren(List.of(
                new Element(Tag.valueOf("meta"), baseUri)
                        .attr(META_PROPERTY, "og:image").attr(META_CONTENT, imageLink)
        ));
        OpenGraph actual = tested.read(document).og();

        Assertions.assertThat(actual).isNotNull()
                .extracting(a -> a.image).isEqualTo(URI.create("http://obiwan.kenobi.jedi/tatooine/featured.jpg"));
    }

    @ParameterizedTest
    @CsvSource({
            "og:image, none",
            "og:url, bad url",
            "og:locale, bad locale"
    })
    void should_build_with_wrong_metas(String tag, String value) {
        Document document = new Document("");
        document.appendChildren(List.of(
                new Element("meta").attr(META_PROPERTY, tag).attr(META_CONTENT, value)
        ));
        OpenGraph actual = tested.read(document).og();
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void should_build_with_null_locale() {
        Document document = new Document("");
        document.appendChildren(List.of(
                new Element("meta").attr(META_PROPERTY, "og:locale")
        ));
        OpenGraph actual = tested.read(document).og();
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void should_build_with_null_url() {
        Document document = new Document("");
        document.appendChildren(List.of(
                new Element("meta").attr(META_PROPERTY, "og:url")
        ));
        OpenGraph actual = tested.read(document).og();
        Assertions.assertThat(actual).isNotNull();
    }
}
