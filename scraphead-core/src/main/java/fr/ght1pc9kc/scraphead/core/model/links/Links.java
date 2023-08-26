package fr.ght1pc9kc.scraphead.core.model.links;

import fr.ght1pc9kc.scraphead.core.model.Header;
import fr.ght1pc9kc.scraphead.core.model.MetaType;
import lombok.Builder;

import java.net.URI;

@Builder
public record Links(
        URI icon,
        URI canonical,
        URI license,
        URI shortlink
) implements Header {
    @Override
    public Links links() {
        return this;
    }

    @Override
    public MetaType metaType() {
        return MetaType.LINK;
    }
}
