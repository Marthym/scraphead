package fr.ght1pc9kc.scraphead.core.model.opengraph;

import fr.ght1pc9kc.scraphead.core.model.Header;
import fr.ght1pc9kc.scraphead.core.model.MetaType;
import lombok.Builder;
import lombok.Generated;
import lombok.With;

import java.net.URI;
import java.net.URL;
import java.util.Locale;

@With
@Builder
public record OpenGraph(
        String title,
        OGType type,
        URL url,
        URI image,
        String description,
        Locale locale
) implements Header {
    public boolean isEmpty() {
        return title == null
                && url == null
                && image == null
                && description == null
                && locale == null;
    }

    @Override
    public OpenGraph openGraph() {
        return this;
    }

    @Override
    public MetaType metaType() {
        return MetaType.OPENGRAPH;
    }

    @Generated
    public static class OpenGraphBuilder {
        // Only to allow javadoc with lombok
    }
}
