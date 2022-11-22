package fr.ght1pc9kc.scraphead.core.scrap;

import fr.ght1pc9kc.scraphead.core.model.Metas;
import fr.ght1pc9kc.scraphead.core.model.links.Links;
import fr.ght1pc9kc.scraphead.core.model.opengraph.OpenGraph;
import fr.ght1pc9kc.scraphead.core.model.twitter.TwitterCard;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.select.NodeFilter;

import java.util.List;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
public final class DocumentMetaReader {
    private static final Set<String> ALLOWED_NODE_NAME = Set.of(
            "#document", "#text", "html", "head", "body", "title", "style", "base", "link", "meta", "script", "noscript");
    private static final Metas EMPTY = Metas.builder().build();
    private final List<MetaDataCollector> collectors;

    public Metas read(Document headDoc) {
        if (collectors.isEmpty()) {
            return EMPTY;
        }

        Metas.MetasBuilder mBuilder = Metas.builder();
        for (MetaDataCollector collector : collectors) {
            Object meta = headDoc
                    .filter((node, depth) -> (ALLOWED_NODE_NAME.contains(node.nodeName())
                            ? NodeFilter.FilterResult.CONTINUE : NodeFilter.FilterResult.REMOVE))
                    .getAllElements()
                    .stream().collect(collector.collector());
            switch (collector.type()) {
                case OPENGRAPH -> mBuilder.og((OpenGraph) meta);
                case TWITTER_CARD -> mBuilder.twitter((TwitterCard) meta);
                case LINK -> mBuilder.links((Links) meta);
                case META -> {
                    String[] strings = (String[]) meta;
                    mBuilder.title(strings[0]).description(strings[1]);
                }
                default -> log.debug("Meta type {} unknown !", collector.type());
            }
        }

        return mBuilder.build();
    }
}
