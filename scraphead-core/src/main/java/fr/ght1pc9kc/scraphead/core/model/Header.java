package fr.ght1pc9kc.scraphead.core.model;

import fr.ght1pc9kc.scraphead.core.model.ex.UnsupportedContentTypeException;
import fr.ght1pc9kc.scraphead.core.model.links.Links;
import fr.ght1pc9kc.scraphead.core.model.opengraph.OpenGraph;
import fr.ght1pc9kc.scraphead.core.model.twitter.TwitterCard;

public interface Header {
    default OpenGraph openGraph() {
        throw new UnsupportedContentTypeException("Not an OpenGraph meta !");
    }

    default TwitterCard twitterCard() {
        throw new UnsupportedContentTypeException("Not an TwitterCard meta !");
    }

    default Links links() {
        throw new UnsupportedContentTypeException("Not an Links meta !");
    }

    default HtmlHead html() {
        throw new UnsupportedContentTypeException("Not a HtmlHead metas !");
    }

    MetaType metaType();
}
