package fr.ght1pc9kc.scraphead.core.model;

import lombok.Builder;

@Builder
public record HtmlHead(
        String title,
        String description
) implements Header {
    @Override
    public HtmlHead html() {
        return this;
    }

    @Override
    public MetaType metaType() {
        return MetaType.HTML;
    }
}
