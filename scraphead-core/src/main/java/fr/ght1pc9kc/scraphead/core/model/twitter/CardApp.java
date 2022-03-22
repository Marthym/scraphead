package fr.ght1pc9kc.scraphead.core.model.twitter;

import java.net.URL;

public record CardApp(
        AppType type,
        String name,
        String id,
        URL url
) {
}
