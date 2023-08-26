package fr.ght1pc9kc.scraphead.core.model.ex;

import lombok.Getter;

@Getter
public class OpenGraphException extends HeadScrapingException {
    private final String tag;

    public OpenGraphException(String tag, String message) {
        super(message);
        this.tag = tag;
    }
}
