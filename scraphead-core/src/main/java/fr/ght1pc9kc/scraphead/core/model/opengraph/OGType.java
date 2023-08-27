package fr.ght1pc9kc.scraphead.core.model.opengraph;

import fr.ght1pc9kc.scraphead.core.model.ex.OpenGraphException;
import org.jetbrains.annotations.NotNull;

public enum OGType {
    ARTICLE, BLOG, BOOK, PROFILE, WEBSITE, VIDEO, VIDEO_MOVIE, VIDEO_OTHER, MUSIC, OBJECT;

    private static final OGType[] ALL = values();

    public static OGType from(@NotNull String value) {
        if (value.isBlank()) {
            throw new IllegalArgumentException();
        }
        String normalized = value.replace('.', '_').toUpperCase();
        for (OGType ogType : ALL) {
            if (ogType.name().equals(normalized)) {
                return ogType;
            }
        }
        throw new OpenGraphException("type", String.format("OGType for %s not found !", value));
    }
}
