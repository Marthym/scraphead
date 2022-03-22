package fr.ght1pc9kc.scraphead.core.model.twitter;

import lombok.Builder;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record TwitterCard(
        @Nullable CardType card,
        @Nullable CardSite site,
        @Nullable CardCreator creator,
        @Nullable String description,
        @Nullable String title,
        List<CardImage> images,
        List<CardApp> apps
) {
    @Builder
    @SuppressWarnings("java:S6207")
    public TwitterCard {
        // Only for lombok builder
    }
}
