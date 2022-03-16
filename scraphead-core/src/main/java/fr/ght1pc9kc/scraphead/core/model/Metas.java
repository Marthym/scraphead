package fr.ght1pc9kc.scraphead.core.model;

import fr.ght1pc9kc.scraphead.core.model.links.Links;
import fr.ght1pc9kc.scraphead.core.model.opengraph.OpenGraph;
import fr.ght1pc9kc.scraphead.core.model.twitter.TwitterCard;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.With;
import org.jetbrains.annotations.Nullable;

@With
@Value
@Builder
@RequiredArgsConstructor
public class Metas {
    @Nullable private Links links;
    @Nullable private OpenGraph og;
    @Nullable private TwitterCard twitter;
}
