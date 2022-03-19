package fr.ght1pc9kc.scraphead.core.scrap;

import fr.ght1pc9kc.scraphead.core.model.MetaType;

import java.util.stream.Collector;

public interface MetaDataCollector {
    MetaType type();

    <T, C> Collector<T, ?, C> collector();
}
