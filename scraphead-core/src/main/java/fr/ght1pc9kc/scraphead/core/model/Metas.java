package fr.ght1pc9kc.scraphead.core.model;

import fr.ght1pc9kc.scraphead.core.model.links.Links;
import fr.ght1pc9kc.scraphead.core.model.opengraph.OpenGraph;
import fr.ght1pc9kc.scraphead.core.model.twitter.TwitterCard;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.With;

@With
@Value
@Builder
@RequiredArgsConstructor
public class Metas {
    private Links links;
    private OpenGraph og;
    private TwitterCard twitter;
}
