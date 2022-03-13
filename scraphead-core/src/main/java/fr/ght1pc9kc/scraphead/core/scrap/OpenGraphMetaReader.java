package fr.ght1pc9kc.scraphead.core.scrap;

import fr.ght1pc9kc.scraphead.core.model.Metas;
import fr.ght1pc9kc.scraphead.core.model.links.Links;
import fr.ght1pc9kc.scraphead.core.model.opengraph.OpenGraph;
import fr.ght1pc9kc.scraphead.core.model.twitter.TwitterCard;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public final class OpenGraphMetaReader {
    private static final Metas EMPTY = Metas.builder().build();
    private final List<MetaDataCollector> collectors;

    public Metas read(Document headDoc) {
        if (collectors.isEmpty()) {
            return EMPTY;
        }

        for (Element element : headDoc.getAllElements()) {
            collectors.forEach(c -> c.inspect(element));
        }

        return collect();
    }

    private Metas collect() {
        Metas.MetasBuilder mBuilder = Metas.builder();

        for (MetaDataCollector c : collectors) {
            switch (c.type()) {
                case OPENGRAPH -> mBuilder.og(c.collect(OpenGraph.class));
                case TWITTER_CARD -> mBuilder.twitter(c.collect(TwitterCard.class));
                case LINK -> mBuilder.links(c.collect(Links.class));
                default -> log.debug("Meta type {} unknown !", c.type());
            }
        }

        return mBuilder.build();
    }
}
