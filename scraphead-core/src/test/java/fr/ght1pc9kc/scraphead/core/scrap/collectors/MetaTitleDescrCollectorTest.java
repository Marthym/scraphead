package fr.ght1pc9kc.scraphead.core.scrap.collectors;

import org.assertj.core.api.Assertions;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static fr.ght1pc9kc.scraphead.core.scrap.OGScrapperUtils.META_CONTENT;
import static fr.ght1pc9kc.scraphead.core.scrap.OGScrapperUtils.META_HREF;
import static fr.ght1pc9kc.scraphead.core.scrap.OGScrapperUtils.META_NAME;
import static fr.ght1pc9kc.scraphead.core.scrap.OGScrapperUtils.META_REL;

class MetaTitleDescrCollectorTest {
    MetaTitleDescrCollector tested = new MetaTitleDescrCollector();

    @Test
    void should_collect_elements_links() {
        String baseUrl = "https://blog.ght1pc9kc.fr/";
        Document document = new Document(baseUrl);
        Tag link = Tag.valueOf("link");
        document.appendChildren(List.of(
                new Element(Tag.valueOf("title"), baseUrl).text("Title de title"),
                new Element(Tag.valueOf("meta"), baseUrl)
                        .attr(META_NAME, "twitter:title").attr(META_CONTENT, "Title de twitter"),
                new Element(link, baseUrl)
                        .attr(META_REL, "canonical").attr(META_HREF, "https://blog.ght1pc9kc.fr/index.html"),
                new Element(link, baseUrl)
                        .attr(META_REL, "icon").attr(META_HREF, "favicon.ico"),
                new Element(Tag.valueOf("meta"), baseUrl)
                        .attr(META_NAME, "description").attr(META_CONTENT, "Description of the description tag"),
                new Element(link, baseUrl)
                        .attr(META_REL, "icon").attr(META_HREF, "favicon.png"),
                new Element(link, baseUrl)
                        .attr(META_REL, "license").attr(META_HREF, "//www.wtfpl.net/"),
                new Element(link, baseUrl)
                        .attr(META_REL, "shortlink").attr(META_HREF, "https://blog.ght1pc9kc.fr/")
        ));

        String[] actual = document.getAllElements().stream().collect(tested);

        Assertions.assertThat(actual).containsExactly(
                "Title de title",
                "Description of the description tag"
        );
    }

    @Test
    @SuppressWarnings("ResultOfMethodCallIgnored")
    void should_collect_parallel() {
        String baseUrl = "https://blog.ght1pc9kc.fr/";
        Document document = new Document(baseUrl);
        Tag link = Tag.valueOf("link");
        document.appendChildren(List.of(
                new Element(Tag.valueOf("title"), baseUrl).text("Title de title"),
                new Element(Tag.valueOf("meta"), baseUrl)
                        .attr(META_NAME, "twitter:title").attr(META_CONTENT, "Title de twitter"),
                new Element(link, baseUrl)
                        .attr(META_REL, "canonical").attr(META_HREF, "https://blog.ght1pc9kc.fr/index.html"),
                new Element(link, baseUrl)
                        .attr(META_REL, "icon").attr(META_HREF, "favicon.ico"),
                new Element(Tag.valueOf("description"), baseUrl).text("Description of the description tag"),
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