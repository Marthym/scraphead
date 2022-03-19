package fr.ght1pc9kc.scraphead.core.scrap.collectors;

import fr.ght1pc9kc.scraphead.core.model.MetaType;
import fr.ght1pc9kc.scraphead.core.model.links.Links;
import fr.ght1pc9kc.scraphead.core.scrap.MetaDataCollector;
import fr.ght1pc9kc.scraphead.core.scrap.OGScrapperUtils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Element;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static fr.ght1pc9kc.scraphead.core.scrap.OGScrapperUtils.META_HREF;
import static fr.ght1pc9kc.scraphead.core.scrap.OGScrapperUtils.META_REL;

@Slf4j
public final class LinksCollector implements MetaDataCollector, Collector<Element, Links.LinksBuilder, Links> {
    private static final String REL_CANONICAL = "canonical";
    private static final String REL_ICON = "icon";
    private static final String REL_LICENSE = "license";
    private static final String REL_SHORTLINK = "shortlink";

    @Override
    public MetaType type() {
        return MetaType.LINK;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T, C> Collector<T, ?, C> collector() {
        return (Collector<T, ?, C>) this;
    }

    @Override
    public Supplier<Links.LinksBuilder> supplier() {
        return Links::builder;
    }

    @Override
    public BiConsumer<Links.LinksBuilder, Element> accumulator() {
        return (builder, element) -> {
            if (!"link".equals(element.tagName()) || !element.hasAttr(META_REL)) {
                return;
            }
            String relation = element.attr(META_REL);

            switch (relation) {
                case REL_CANONICAL -> OGScrapperUtils.toUri(element.attr("abs:" + META_HREF))
                        .ifPresent(builder::canonical);
                case REL_ICON -> OGScrapperUtils.toUri(element.attr("abs:" + META_HREF))
                        .ifPresent(builder::icon);
                case REL_LICENSE -> OGScrapperUtils.toUri(element.attr("abs:" + META_HREF))
                        .ifPresent(builder::license);
                case REL_SHORTLINK -> OGScrapperUtils.toUri(element.attr("abs:" + META_HREF))
                        .ifPresent(builder::shortlink);
                default -> log.trace("Unmanaged relation for {}", relation);
            }
        };
    }

    @Override
    public BinaryOperator<Links.LinksBuilder> combiner() {
        return (left, right) -> {
            throw new IllegalStateException("Unable to combine Links Builder !");
        };
    }

    @Override
    public Function<Links.LinksBuilder, Links> finisher() {
        return Links.LinksBuilder::build;
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Set.of(Characteristics.UNORDERED);
    }
}
