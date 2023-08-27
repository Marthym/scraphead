package fr.ght1pc9kc.scraphead.core.scrap;

import fr.ght1pc9kc.scraphead.core.model.Header;
import fr.ght1pc9kc.scraphead.core.model.HtmlHead;
import fr.ght1pc9kc.scraphead.core.model.Metas;
import fr.ght1pc9kc.scraphead.core.scrap.collectors.MetaDataCollector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.select.NodeFilter;

import java.net.URI;
import java.util.List;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
public final class DocumentMetaReader {
    private static final Set<String> ALLOWED_NODE_NAME = Set.of(
            "#document", "#text", "html", "head", "body", "title", "style", "base", "link", "meta", "script", "noscript");
    private final List<MetaDataCollector<? extends Header>> collectors;

    public Metas read(URI resourceUrl, Document headDoc) {
        if (collectors.isEmpty()) {
            return Metas.builder().resourceUrl(resourceUrl).build();
        }

        Metas.MetasBuilder mBuilder = Metas.builder()
                .resourceUrl(resourceUrl);
        for (var collector : collectors) {
            var meta = headDoc
                    .filter((node, depth) -> (ALLOWED_NODE_NAME.contains(node.nodeName())
                            ? NodeFilter.FilterResult.CONTINUE : NodeFilter.FilterResult.REMOVE))
                    .getAllElements()
                    .stream().collect(collector.collector());
            switch (meta.object().metaType()) {
                case OPENGRAPH -> mBuilder.og(meta.object().openGraph());
                case TWITTER_CARD -> mBuilder.twitter(meta.object().twitterCard());
                case LINK -> mBuilder.links(meta.object().links());
                case HTML -> {
                    HtmlHead htmlHead = meta.object().html();
                    mBuilder.title(htmlHead.title()).description(htmlHead.description());
                }
                default -> log.debug("Meta type {} unknown !", meta.object().metaType());
            }
            meta.errors().forEach(mBuilder::error);
        }

        return mBuilder.build();
    }
}
