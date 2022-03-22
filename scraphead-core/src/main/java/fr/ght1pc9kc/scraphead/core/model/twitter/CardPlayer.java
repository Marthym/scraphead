package fr.ght1pc9kc.scraphead.core.model.twitter;

import org.jetbrains.annotations.Nullable;

import java.net.URL;

public record CardPlayer(
        URL url,
        int width,
        int height,
        @Nullable URL stream
) {
    public static CardPlayer of(URL url) {
        return new CardPlayer(url, -1, -1, null);
    }
}
