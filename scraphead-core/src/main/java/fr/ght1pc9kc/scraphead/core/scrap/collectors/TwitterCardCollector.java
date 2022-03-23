package fr.ght1pc9kc.scraphead.core.scrap.collectors;

import fr.ght1pc9kc.scraphead.core.model.MetaType;
import fr.ght1pc9kc.scraphead.core.model.twitter.CardApp;
import fr.ght1pc9kc.scraphead.core.model.twitter.CardCreator;
import fr.ght1pc9kc.scraphead.core.model.twitter.CardImage;
import fr.ght1pc9kc.scraphead.core.model.twitter.CardPlayer;
import fr.ght1pc9kc.scraphead.core.model.twitter.CardSite;
import fr.ght1pc9kc.scraphead.core.model.twitter.CardType;
import fr.ght1pc9kc.scraphead.core.model.twitter.TwitterCard;
import fr.ght1pc9kc.scraphead.core.scrap.MetaDataCollector;
import fr.ght1pc9kc.scraphead.core.scrap.ScrapperUtils;
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

import static fr.ght1pc9kc.scraphead.core.scrap.ScrapperUtils.META_CONTENT;
import static fr.ght1pc9kc.scraphead.core.scrap.ScrapperUtils.META_NAME;

@Slf4j
public class TwitterCardCollector implements MetaDataCollector, Collector<Element, TwitterCard.TwitterCardBuilder, TwitterCard> {
    private static final String TWITTER_NAMESPACE = "twitter:";
    private static final String TWITTER_CARD = TWITTER_NAMESPACE + "card";
    private static final String TWITTER_SITE = TWITTER_NAMESPACE + "site";
    private static final String TWITTER_SITE_ID = TWITTER_SITE + ":id";
    private static final String TWITTER_CREATOR = TWITTER_NAMESPACE + "creator";
    private static final String TWITTER_CREATOR_ID = TWITTER_CREATOR + ":id";
    private static final String TWITTER_TITLE = TWITTER_NAMESPACE + "title";
    private static final String TWITTER_IMAGE = TWITTER_NAMESPACE + "image";
    private static final String TWITTER_IMAGE_ALT = TWITTER_IMAGE + ":alt";
    private static final String TWITTER_PLAYER = TWITTER_NAMESPACE + "player";
    private static final String TWITTER_PLAYER_WIDTH = TWITTER_PLAYER + ":width";
    private static final String TWITTER_PLAYER_HEIGHT = TWITTER_PLAYER + ":height";
    private static final String TWITTER_PLAYER_STREAM = TWITTER_PLAYER + ":stream";

    private static final String TWITTER_URL = TWITTER_NAMESPACE + "url";
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

            List<CardApp> apps = new ArrayList<>();
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
                case TWITTER_IMAGE:
                    ScrapperUtils.toUrl(element.attr("abs:" + META_CONTENT))
                            .map(u -> {
                                CardImage img = builder.build().image();
                                if (img == null) {
                                    return CardImage.of(u);
                                } else {
                                    return img.withUrl(u);
                                }
                            })
                            .ifPresent(builder::image);
                    break;
                case TWITTER_IMAGE_ALT:
                    String alt = element.attr(META_CONTENT);
                    if (!alt.isBlank()) {
                        CardImage img = builder.build().image();
                        if (img == null) {
                            builder.image(new CardImage(null, alt));
                        } else {
                            builder.image(img.withAlt(alt));
                        }
                    }
                    break;
                case TWITTER_PLAYER:
                    ScrapperUtils.toUrl(element.attr("abs:" + META_CONTENT))
                            .map(u -> {
                                CardPlayer player = builder.build().player();
                                if (player == null) {
                                    return CardPlayer.of(u);
                                } else {
                                    return player.withUrl(u);
                                }
                            })
                            .ifPresent(builder::player);
                    break;
                case TWITTER_PLAYER_WIDTH:
                    String width = element.attr(META_CONTENT);
                    if (!width.isBlank()) {
                        CardPlayer player = builder.build().player();
                        if (player == null) {
                            builder.player(new CardPlayer(null, Integer.parseInt(width), -1, null));
                        } else {
                            builder.player(player.withWidth(Integer.parseInt(width)));
                        }
                    }
                    break;
                case TWITTER_PLAYER_HEIGHT:
                    String height = element.attr(META_CONTENT);
                    if (!height.isBlank()) {
                        CardPlayer player = builder.build().player();
                        if (player == null) {
                            builder.player(new CardPlayer(null, -1, Integer.parseInt(height), null));
                        } else {
                            builder.player(player.withWidth(Integer.parseInt(height)));
                        }
                    }
                    break;
                case TWITTER_PLAYER_STREAM:
                    ScrapperUtils.toUrl(element.attr("abs:" + META_CONTENT))
                            .map(u -> {
                                CardPlayer player = builder.build().player();
                                if (player == null) {
                                    return new CardPlayer(null, -1, -1, u);
                                } else {
                                    return player.withUrl(u);
                                }
                            })
                            .ifPresent(builder::player);
                    break;
                default:
            }
            builder.apps(List.copyOf(apps));
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
