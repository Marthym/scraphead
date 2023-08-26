package fr.ght1pc9kc.scraphead.core.scrap.collectors;

import fr.ght1pc9kc.scraphead.core.model.links.Links;
import fr.ght1pc9kc.scraphead.core.scrap.OGScrapperUtils;
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

import static fr.ght1pc9kc.scraphead.core.scrap.OGScrapperUtils.META_HREF;
import static fr.ght1pc9kc.scraphead.core.scrap.OGScrapperUtils.META_REL;

@Slf4j
public final class LinksCollector implements MetaDataCollector<Links>, Collector<Element, WithErrors<Links.LinksBuilder>, WithErrors<Links>> {
    private static final String REL_CANONICAL = "canonical";
    private static final String REL_ICON = "icon";
    private static final String REL_LICENSE = "license";
    private static final String REL_SHORTLINK = "shortlink";

    @Override
    public Collector<Element, WithErrors<Links.LinksBuilder>, WithErrors<Links>> collector() {
        return this;
    }

    @Override
    public Supplier<WithErrors<Links.LinksBuilder>> supplier() {
        return () -> new WithErrors<>(Links.builder(), new ArrayList<>());
    }

    @Override
    public BiConsumer<WithErrors<Links.LinksBuilder>, Element> accumulator() {
        return (builder, element) -> {
            if (!"link".equals(element.tagName()) || !element.hasAttr(META_REL)) {
                return;
            }
            String relation = element.attr(META_REL);

            switch (relation) {
                case REL_CANONICAL -> OGScrapperUtils.toUri(element.attr("abs:" + META_HREF))
                        .ifPresent(builder.object()::canonical);
                case REL_ICON -> OGScrapperUtils.toUri(element.attr("abs:" + META_HREF))
                        .ifPresent(builder.object()::icon);
                case REL_LICENSE -> OGScrapperUtils.toUri(element.attr("abs:" + META_HREF))
                        .ifPresent(builder.object()::license);
                case REL_SHORTLINK -> OGScrapperUtils.toUri(element.attr("abs:" + META_HREF))
                        .ifPresent(builder.object()::shortlink);
                default -> log.trace("Unmanaged relation for {}", relation);
            }
        };
    }

    @Override
    public BinaryOperator<WithErrors<Links.LinksBuilder>> combiner() {
        return (left, right) -> {
            throw new IllegalStateException("Unable to combine Links Builder !");
        };
    }

    @Override
    public Function<WithErrors<Links.LinksBuilder>, WithErrors<Links>> finisher() {
        return builder -> new WithErrors<>(builder.object().build(), List.copyOf(builder.errors()));
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Set.of(Characteristics.UNORDERED);
    }
}
