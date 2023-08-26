package fr.ght1pc9kc.scraphead.core.scrap.collectors;

import fr.ght1pc9kc.scraphead.core.model.Header;
import org.jsoup.nodes.Element;

import java.util.stream.Collector;

public interface MetaDataCollector<C extends Header> {
    Collector<Element, ?, WithErrors<C>> collector();
}
