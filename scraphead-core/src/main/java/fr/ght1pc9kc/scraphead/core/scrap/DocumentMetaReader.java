package fr.ght1pc9kc.scraphead.core.scrap;

import fr.ght1pc9kc.scraphead.core.model.Metas;
import fr.ght1pc9kc.scraphead.core.model.links.Links;
import fr.ght1pc9kc.scraphead.core.model.opengraph.OpenGraph;
import fr.ght1pc9kc.scraphead.core.model.twitter.TwitterCard;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public final class DocumentMetaReader {
    private static final Metas EMPTY = Metas.builder().build();
    private final List<MetaDataCollector> collectors;

    public Metas read(Document headDoc) {
        if (collectors.isEmpty()) {
            return EMPTY;
        }

        Metas.MetasBuilder mBuilder = Metas.builder();
        for (MetaDataCollector collector : collectors) {
            Object meta = headDoc.getAllElements().stream().collect(collector.collector());
            switch (collector.type()) {
                case OPENGRAPH -> mBuilder.og((OpenGraph) meta);
                case TWITTER_CARD -> mBuilder.twitter((TwitterCard) meta);
                case LINK -> mBuilder.links((Links) meta);
                default -> log.debug("Meta type {} unknown !", collector.type());
            }
        }

        return mBuilder.build();
    }
}
