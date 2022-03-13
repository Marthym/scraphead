package fr.ght1pc9kc.scraphead.core.model.opengraph;

import lombok.Builder;
import lombok.Value;
import lombok.With;

import java.net.URI;
import java.net.URL;
import java.util.Locale;

@With
@Value
@Builder
@SuppressWarnings("java:S1104")
public class OpenGraph {
    public String title;
    public OGType type;
    public URL url;
    public URI image;
    public String description;
    public Locale locale;

    public boolean isEmpty() {
        return title == null
                && url == null
                && image == null
                && description == null
                && locale == null;
    }
}
