package fr.ght1pc9kc.scraphead.core.scrap.collectors;

import fr.ght1pc9kc.scraphead.core.model.MetaType;
import fr.ght1pc9kc.scraphead.core.model.twitter.CardCreator;
import fr.ght1pc9kc.scraphead.core.model.twitter.CardSite;
import fr.ght1pc9kc.scraphead.core.model.twitter.CardType;
import fr.ght1pc9kc.scraphead.core.model.twitter.TwitterCard;
import fr.ght1pc9kc.scraphead.core.scrap.MetaDataCollector;
import fr.ght1pc9kc.scraphead.core.scrap.OGScrapperUtils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Element;

import java.util.Locale;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static fr.ght1pc9kc.scraphead.core.scrap.OGScrapperUtils.META_CONTENT;
import static fr.ght1pc9kc.scraphead.core.scrap.OGScrapperUtils.META_NAME;

@Slf4j
public class TwitterCardCollector implements MetaDataCollector, Collector<Element, TwitterCard.TwitterCardBuilder, TwitterCard> {
    private static final String TWITTER_NAMESPACE = "twitter:";
    private static final String TWITTER_CARD = TWITTER_NAMESPACE + "card";
    private static final String TWITTER_SITE = TWITTER_NAMESPACE + "site";
    private static final String TWITTER_SITE_ID = TWITTER_SITE + ":id";
    private static final String TWITTER_CREATOR = TWITTER_NAMESPACE + "creator";
    private static final String TWITTER_CREATOR_ID = TWITTER_CREATOR + ":id";
    private static final String TWITTER_TITLE = TWITTER_NAMESPACE + "title";
    private static final String TWITTER_URL = TWITTER_NAMESPACE + "url";
    private static final String TWITTER_IMAGE = TWITTER_NAMESPACE + "image";
    private static final String TWITTER_DESCRIPTION = TWITTER_NAMESPACE + "description";
    private static final String TWITTER_LOCALE = TWITTER_NAMESPACE + "locale";
    public static final String EXCEPTION_BASE_MESSAGE = "{}: {}";

    @Override
    public MetaType type() {
        return MetaType.TWITTER_CARD;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T, C> Collector<T, ?, C> collector() {
        return (Collector<T, ?, C>) this;
    }

    @Override
    public Supplier<TwitterCard.TwitterCardBuilder> supplier() {
        return TwitterCard::builder;
    }

    @Override
    public BiConsumer<TwitterCard.TwitterCardBuilder, Element> accumulator() {
        return (builder, element) -> {
            if (!"meta".equals(element.tagName())) {
                return;
            }
            String property = element.attr(META_NAME);
            if (!property.startsWith(TWITTER_NAMESPACE)) {
                return;
            }

            switch (property) {
                case TWITTER_CARD:
                    try {
                        builder.card(CardType.valueOf(element.attr(META_CONTENT).toUpperCase()));
                    } catch (Exception e) {
                        log.warn("Unable to read card value from {}", element);
                        log.warn(EXCEPTION_BASE_MESSAGE, e.getClass(), e.getLocalizedMessage());
                    }
                    break;
                case TWITTER_SITE:
                    builder.site(CardSite.of(element.attr(META_CONTENT)));
                    break;
                case TWITTER_SITE_ID:
                    try {
                        builder.site(CardSite.of(Long.parseLong(element.attr(META_CONTENT))));
                    } catch (Exception e) {
                        log.warn("Unable to read site id as long from {}", element);
                        log.warn(EXCEPTION_BASE_MESSAGE, e.getClass(), e.getLocalizedMessage());
                    }
                    break;
                case TWITTER_CREATOR:
                    builder.creator(CardCreator.of(element.attr(META_CONTENT)));
                    break;
                case TWITTER_CREATOR_ID:
                    try {
                        builder.creator(CardCreator.of(Long.parseLong(element.attr(META_CONTENT))));
                    } catch (Exception e) {
                        log.warn("Unable to read site id as long from {}", element);
                        log.warn(EXCEPTION_BASE_MESSAGE, e.getClass(), e.getLocalizedMessage());
                    }
                    break;
                case TWITTER_TITLE:
                    builder.title(element.attr(META_CONTENT));
                    break;
                case TWITTER_URL:
                    OGScrapperUtils.toUrl(element.attr("abs:" + META_CONTENT))
                            .ifPresent(builder::url);
                    break;
                case TWITTER_IMAGE:
                    OGScrapperUtils.toUri(element.attr("abs:" + META_CONTENT))
                            .ifPresent(builder::image);
                    break;
                case TWITTER_DESCRIPTION:
                    if (element.hasAttr(META_CONTENT)) {
                        builder.description(element.attr(META_CONTENT));
                    }
                    break;
                case TWITTER_LOCALE:
                    if (element.hasAttr(META_CONTENT)) {
                        builder.locale(Locale.forLanguageTag(
                                element.attr(META_CONTENT).replace('_', '-')));
                    }
                    break;
                default:
            }
        };
    }

    @Override
    public BinaryOperator<TwitterCard.TwitterCardBuilder> combiner() {
        return (left, right) -> {
            throw new IllegalStateException("Unable to combine TwitterCard Builder !");
        };
    }

    @Override
    public Function<TwitterCard.TwitterCardBuilder, TwitterCard> finisher() {
        return TwitterCard.TwitterCardBuilder::build;
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Set.of(Characteristics.UNORDERED);
    }
}
