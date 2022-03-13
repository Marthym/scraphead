package fr.ght1pc9kc.scraphead.core.scrap;

import fr.ght1pc9kc.scraphead.core.scrap.model.MetaType;
import org.jsoup.nodes.Element;

public interface MetaDataCollector {
    MetaType type();

    void inspect(Element element);

    <T> T collect(Class<T> clazz);
}
