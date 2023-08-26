package fr.ght1pc9kc.scraphead.core.model;

import fr.ght1pc9kc.scraphead.core.model.ex.HeadScrapingException;
import fr.ght1pc9kc.scraphead.core.model.links.Links;
import fr.ght1pc9kc.scraphead.core.model.opengraph.OpenGraph;
import fr.ght1pc9kc.scraphead.core.model.twitter.TwitterCard;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;

import java.net.URI;
import java.util.Collection;

/**
 * All the metas from the submitted URI
 *
 * @param resourceUrl The resourceUrl. In case of redirection, this is the last URL
 * @param links       The links present in page {@code <head>}.
 * @param og          The OpenGraph page headers
 * @param twitter     The Twitter page headers
 * @param title       The title of the page
 * @param description The description tag for the page
 * @param errors      All the parsing errors
 */
@Builder
public record Metas(
        @NonNull URI resourceUrl,
        Links links,
        OpenGraph og,
        TwitterCard twitter,
        String title,
        String description,
        @Singular Collection<HeadScrapingException> errors
) {
}
