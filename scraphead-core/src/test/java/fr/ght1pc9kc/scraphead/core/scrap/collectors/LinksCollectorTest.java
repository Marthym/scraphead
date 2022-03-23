package fr.ght1pc9kc.scraphead.core.scrap.collectors;

import fr.ght1pc9kc.scraphead.core.model.links.Links;
import org.assertj.core.api.Assertions;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;
import java.util.stream.Stream;

import static fr.ght1pc9kc.scraphead.core.scrap.ScrapperUtils.META_CONTENT;
import static fr.ght1pc9kc.scraphead.core.scrap.ScrapperUtils.META_HREF;
import static fr.ght1pc9kc.scraphead.core.scrap.ScrapperUtils.META_NAME;
import static fr.ght1pc9kc.scraphead.core.scrap.ScrapperUtils.META_REL;

class LinksCollectorTest {

    LinksCollector tested = new LinksCollector();

    @Test
    void should_collect_elements_links() {
        String baseUrl = "https://blog.ght1pc9kc.fr/";
        Document document = new Document(baseUrl);
        Tag link = Tag.valueOf("link");
        document.appendChildren(List.of(
                new Element(Tag.valueOf("meta"), baseUrl)
                        .attr(META_NAME, "twitter:title").attr(META_CONTENT, "Title de twitter"),
                new Element(link, baseUrl)
                        .attr(META_REL, "canonical").attr(META_HREF, "https://blog.ght1pc9kc.fr/index.html"),
                new Element(link, baseUrl)
                        .attr(META_REL, "icon").attr(META_HREF, "favicon.ico"),
                new Element(link, baseUrl)
                        .attr(META_REL, "icon").attr(META_HREF, "favicon.png"),
                new Element(link, baseUrl)
                        .attr(META_REL, "license").attr(META_HREF, "//www.wtfpl.net/"),
                new Element(link, baseUrl)
                        .attr(META_REL, "shortlink").attr(META_HREF, "https://blog.ght1pc9kc.fr/")
        ));

        Links actual = document.getAllElements().stream().collect(tested);

        Assertions.assertThat(actual).isEqualTo(Links.builder()
                .canonical(URI.create("https://blog.ght1pc9kc.fr/index.html"))
                .icon(URI.create("https://blog.ght1pc9kc.fr/favicon.png"))
                .license(URI.create("https://www.wtfpl.net/"))
                .shortlink(URI.create("https://blog.ght1pc9kc.fr/"))
                .build());
    }

    @Test
    @SuppressWarnings("ResultOfMethodCallIgnored")
    void should_collect_parallel() {
        String baseUrl = "https://blog.ght1pc9kc.fr/";
        Document document = new Document(baseUrl);
        Tag link = Tag.valueOf("link");
        document.appendChildren(List.of(
                new Element(Tag.valueOf("meta"), baseUrl)
                        .attr(META_NAME, "twitter:title").attr(META_CONTENT, "Title de twitter"),
                new Element(link, baseUrl)
                        .attr(META_REL, "canonical").attr(META_HREF, "https://blog.ght1pc9kc.fr/index.html"),
                new Element(link, baseUrl)
                        .attr(META_REL, "icon").attr(META_HREF, "favicon.ico"),
                new Element(link, baseUrl)
                        .attr(META_REL, "icon").attr(META_HREF, "favicon.png"),
                new Element(link, baseUrl)
                        .attr(META_REL, "license").attr(META_HREF, "//www.wtfpl.net/"),
                new Element(link, baseUrl)
                        .attr(META_REL, "shortlink").attr(META_HREF, "https://blog.ght1pc9kc.fr/")
        ));

        Stream<Element> stream = document.getAllElements().stream().parallel();
        Assertions.assertThatThrownBy(() -> stream.collect(tested))
                .isInstanceOf(IllegalStateException.class);
    }
}