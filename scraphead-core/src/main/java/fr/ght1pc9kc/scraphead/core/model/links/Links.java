package fr.ght1pc9kc.scraphead.core.model.links;

import java.net.URI;

public record Links(
        URI icon,
        URI canonical,
        URI license,
        URI shortlink
) {
}
