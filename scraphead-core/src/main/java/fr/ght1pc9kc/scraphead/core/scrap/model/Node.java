package fr.ght1pc9kc.scraphead.core.scrap.model;

import java.util.Map;

public record Node(
        String tag,
        Map<String, String> attr
) {
}
