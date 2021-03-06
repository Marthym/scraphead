package fr.ght1pc9kc.scraphead.core.model.links;

import lombok.Builder;
import lombok.Generated;

import java.net.URI;

public record Links(
        URI icon,
        URI canonical,
        URI license,
        URI shortlink
) {
    @Builder
    @SuppressWarnings("java:S6207")
    public Links {
        // Empty constructor for Lombok Builder
    }

    @Generated
    public static class LinksBuilder {
        // Only to allow javadoc with lombok
    }
}
