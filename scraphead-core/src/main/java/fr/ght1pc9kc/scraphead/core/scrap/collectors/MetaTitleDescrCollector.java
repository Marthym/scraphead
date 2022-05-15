package fr.ght1pc9kc.scraphead.core.scrap.collectors;

import fr.ght1pc9kc.scraphead.core.model.MetaType;
import fr.ght1pc9kc.scraphead.core.scrap.MetaDataCollector;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Element;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

@Slf4j
public final class MetaTitleDescrCollector implements MetaDataCollector, Collector<Element, String[], String[]> {
    private static final String TAG_TITLE = "title";
    private static final String TAG_DESCRIPTION = "meta";
    private static final String ATTR_DESCRIPTION = "name";
    private static final String VAL_DESCRIPTION = "description";
    private static final String ATTR_CONTENT = "CONTENT";

    @Override
    public MetaType type() {
        return MetaType.META;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T, C> Collector<T, ?, C> collector() {
        return (Collector<T, ?, C>) this;
    }

    @Override
    public Supplier<String[]> supplier() {
        return () -> new String[2];
    }

    @Override
    public BiConsumer<String[], Element> accumulator() {
        return (builder, element) -> {
            if (TAG_TITLE.equals(element.tagName())) {
                builder[0] = element.ownText();
            } else if (isDescription(element)) {
                builder[1] = element.attr(ATTR_CONTENT);
            }
        };
    }

    @Override
    public BinaryOperator<String[]> combiner() {
        return (left, right) -> {
            throw new IllegalStateException("Unable to combine Links Builder !");
        };
    }

    @Override
    public Function<String[], String[]> finisher() {
        return Function.identity();
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Set.of(Characteristics.UNORDERED, Characteristics.IDENTITY_FINISH);
    }

    private boolean isDescription(Element el) {
        return TAG_DESCRIPTION.equals(el.tagName())
                && el.hasAttr(ATTR_DESCRIPTION)
                && el.attr(ATTR_DESCRIPTION).equals(VAL_DESCRIPTION)
                && el.hasAttr(ATTR_CONTENT);
    }
}
