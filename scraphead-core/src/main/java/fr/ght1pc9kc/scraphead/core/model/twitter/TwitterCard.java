package fr.ght1pc9kc.scraphead.core.model.twitter;

import fr.ght1pc9kc.scraphead.core.model.Header;
import fr.ght1pc9kc.scraphead.core.model.MetaType;

public record TwitterCard() implements Header {
    @Override
    public TwitterCard twitterCard() {
        return this;
    }

    @Override
    public MetaType metaType() {
        return MetaType.TWITTER_CARD;
    }
}
