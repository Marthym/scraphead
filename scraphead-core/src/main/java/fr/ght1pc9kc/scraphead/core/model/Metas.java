package fr.ght1pc9kc.scraphead.core.model;

import fr.ght1pc9kc.scraphead.core.model.links.Links;
import fr.ght1pc9kc.scraphead.core.model.opengraph.OpenGraph;
import fr.ght1pc9kc.scraphead.core.model.twitter.TwitterCard;
import lombok.Builder;

public record Metas(
        Links links,
        OpenGraph og,
        TwitterCard twitter,
        String title,
        String description
) {
    @Builder
    @SuppressWarnings("java:S6207")
    public Metas {
        // Only for @Builder
    }
}
