package fr.ght1pc9kc.scraphead.core.model.ex;

public class HeadScrapingException extends RuntimeException {
    public HeadScrapingException(String message) {
        super(message);
    }

    public HeadScrapingException(String message, Throwable cause) {
        super(message, cause);
    }
}
