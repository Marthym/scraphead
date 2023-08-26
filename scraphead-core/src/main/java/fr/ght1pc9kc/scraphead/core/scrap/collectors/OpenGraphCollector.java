package fr.ght1pc9kc.scraphead.core.scrap.collectors;

import fr.ght1pc9kc.scraphead.core.model.ex.OpenGraphException;
import fr.ght1pc9kc.scraphead.core.model.opengraph.OGType;
import fr.ght1pc9kc.scraphead.core.model.opengraph.OpenGraph;
import fr.ght1pc9kc.scraphead.core.scrap.OGScrapperUtils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static fr.ght1pc9kc.scraphead.core.scrap.OGScrapperUtils.META_CONTENT;
import static fr.ght1pc9kc.scraphead.core.scrap.OGScrapperUtils.META_NAME;
import static fr.ght1pc9kc.scraphead.core.scrap.OGScrapperUtils.META_PROPERTY;

@Slf4j
public final class OpenGraphCollector implements MetaDataCollector<OpenGraph>,
        Collector<Element, WithErrors<OpenGraph.OpenGraphBuilder>, WithErrors<OpenGraph>> {
    private static final String OG_NAMESPACE = "og:";
    private static final String OG_TITLE = OG_NAMESPACE + "title";
    private static final String OG_TYPE = OG_NAMESPACE + "type";
    private static final String OG_URL = OG_NAMESPACE + "url";
    private static final String OG_IMAGE = OG_NAMESPACE + "image";
    private static final String OG_DESCRIPTION = OG_NAMESPACE + "description";
    private static final String OG_LOCALE = OG_NAMESPACE + "locale";

    @Override
    public Collector<Element, WithErrors<OpenGraph.OpenGraphBuilder>, WithErrors<OpenGraph>> collector() {
        return this;
    }

    @Override
    public Supplier<WithErrors<OpenGraph.OpenGraphBuilder>> supplier() {
        return () -> new WithErrors<>(OpenGraph.builder(), new ArrayList<>());
    }

    @Override
    public BiConsumer<WithErrors<OpenGraph.OpenGraphBuilder>, Element> accumulator() {
        return (builderWithErrors, element) -> {
            if (!"meta".equals(element.tagName())) {
                return;
            }
            String property = (element.hasAttr(META_PROPERTY))
                    ? element.attr(META_PROPERTY) : element.attr(META_NAME);

            switch (property) {
                case OG_TITLE -> builderWithErrors.object().title(element.attr(META_CONTENT));
                case OG_TYPE -> {
                    try {
                        builderWithErrors.object().type(OGType.from(element.attr(META_CONTENT)));
                    } catch (OpenGraphException e) {
                        builderWithErrors.errors().add(e);
                    }
                }
                case OG_URL -> OGScrapperUtils.toUrl(element.attr("abs:" + META_CONTENT))
                        .ifPresent(builderWithErrors.object()::url);
                case OG_IMAGE -> OGScrapperUtils.toUri(element.attr("abs:" + META_CONTENT))
                        .ifPresent(builderWithErrors.object()::image);
                case OG_DESCRIPTION -> {
                    if (element.hasAttr(META_CONTENT)) {
                        builderWithErrors.object().description(element.attr(META_CONTENT));
                    }
                }
                case OG_LOCALE -> {
                    if (element.hasAttr(META_CONTENT)) {
                        builderWithErrors.object().locale(Locale.forLanguageTag(
                                element.attr(META_CONTENT).replace('_', '-')));
                    }
                }
                default -> log.atTrace().log("Unknown property {}", property);
            }
        };
    }

    @Override
    public BinaryOperator<WithErrors<OpenGraph.OpenGraphBuilder>> combiner() {
        return (left, right) -> {
            throw new IllegalStateException("Unable to combine OpenGraph Builder !");
        };
    }

    @Override
    public Function<WithErrors<OpenGraph.OpenGraphBuilder>, WithErrors<OpenGraph>> finisher() {
        return builderWithErrors -> new WithErrors<>(builderWithErrors.object().build(), List.copyOf(builderWithErrors.errors()));
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Set.of();
    }
}
