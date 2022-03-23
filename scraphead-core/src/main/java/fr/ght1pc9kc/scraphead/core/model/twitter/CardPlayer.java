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

    public CardPlayer withUrl(URL u) {
        return new CardPlayer(u, this.width, this.height, this.stream);
    }

    public CardPlayer withWidth(int w) {
        return new CardPlayer(this.url, w, this.height, this.stream);
    }

    public CardPlayer withHeight(int h) {
        return new CardPlayer(this.url, this.width, h, this.stream);
    }

    public CardPlayer withStream(URL s) {
        return new CardPlayer(this.url, this.width, this.height, s);
    }
}
