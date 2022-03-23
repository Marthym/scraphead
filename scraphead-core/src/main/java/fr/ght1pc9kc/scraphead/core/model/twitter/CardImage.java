package fr.ght1pc9kc.scraphead.core.model.twitter;

import org.jetbrains.annotations.Nullable;

import java.net.URL;

public record CardImage(
        URL url,
        @Nullable String alt
) {
    public static CardImage of(URL url) {
        return new CardImage(url, null);
    }

    public static CardImage of(URL url, String alt) {
        return new CardImage(url, alt);
    }

    public CardImage withAlt(String alt) {
        return CardImage.of(this.url, alt);
    }

    public CardImage withUrl(URL url) {
        return CardImage.of(url, this.alt);
    }
}
