package fr.ght1pc9kc.scraphead.core.scrap.collectors;

import fr.ght1pc9kc.scraphead.core.model.HtmlHead;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

@Slf4j
public final class HtmlHeadCollector
        implements MetaDataCollector<HtmlHead>, Collector<Element, WithErrors<HtmlHead.HtmlHeadBuilder>, WithErrors<HtmlHead>> {
    private static final String TAG_TITLE = "title";
    private static final String TAG_DESCRIPTION = "meta";
    private static final String ATTR_DESCRIPTION = "name";
    private static final String VAL_DESCRIPTION = "description";
    private static final String ATTR_CONTENT = "CONTENT";

    @Override
    public Collector<Element, WithErrors<HtmlHead.HtmlHeadBuilder>, WithErrors<HtmlHead>> collector() {
        return this;
    }

    @Override
    public Supplier<WithErrors<HtmlHead.HtmlHeadBuilder>> supplier() {
        return () -> new WithErrors<>(HtmlHead.builder(), new ArrayList<>());
    }

    @Override
    public BiConsumer<WithErrors<HtmlHead.HtmlHeadBuilder>, Element> accumulator() {
        return (builder, element) -> {
            if (TAG_TITLE.equals(element.tagName())) {
                builder.object().title(element.ownText());
            } else if (isDescription(element)) {
                builder.object().description(element.attr(ATTR_CONTENT));
            }
        };
    }

    @Override
    public BinaryOperator<WithErrors<HtmlHead.HtmlHeadBuilder>> combiner() {
        return (left, right) -> {
            throw new IllegalStateException("Unable to combine HtmlHead Builder !");
        };
    }

    @Override
    public Function<WithErrors<HtmlHead.HtmlHeadBuilder>, WithErrors<HtmlHead>> finisher() {
        return builder -> new WithErrors<>(builder.object().build(), List.copyOf(builder.errors()));
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Set.of(Characteristics.UNORDERED);
    }

    private boolean isDescription(Element el) {
        return TAG_DESCRIPTION.equals(el.tagName())
                && el.hasAttr(ATTR_DESCRIPTION)
                && el.attr(ATTR_DESCRIPTION).equals(VAL_DESCRIPTION)
                && el.hasAttr(ATTR_CONTENT);
    }
}
