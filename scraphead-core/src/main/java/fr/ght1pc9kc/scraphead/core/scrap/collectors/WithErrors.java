package fr.ght1pc9kc.scraphead.core.scrap.collectors;

import fr.ght1pc9kc.scraphead.core.model.ex.HeadScrapingException;

import java.util.Collection;

public record WithErrors<T>(
        T object,
        Collection<HeadScrapingException> errors
) {
}
