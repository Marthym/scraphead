package fr.ght1pc9kc.scraphead.core.scrap;

import fr.ght1pc9kc.scraphead.core.model.Meta;
import fr.ght1pc9kc.scraphead.core.model.OGType;
import fr.ght1pc9kc.scraphead.core.model.OpenGraph;
import fr.ght1pc9kc.scraphead.core.model.Tags;
import lombok.extern.slf4j.Slf4j;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Locale;
import java.util.Optional;

@Slf4j
public final class OpenGraphMetaReader {

    public OpenGraph read(Collection<Meta> metas) {
        URI location = metas.stream()
                .filter(m -> Tags.OG_URL.equals(m.property()))
                .findAny()
                .flatMap(m -> {
                    try {
                        return Optional.of(URI.create(m.content()));
                    } catch (Exception e) {
                        return Optional.empty();
                    }
                }).orElse(null);
        return read(metas, location);
    }

    public OpenGraph read(Collection<Meta> metas, URI location) {
        OpenGraph.OpenGraphBuilder builder = OpenGraph.builder();
        for (Meta m : metas) {
            switch (m.property()) {
                case Tags.OG_TITLE:
                    builder.title(m.content());
                    break;
                case Tags.OG_TYPE:
                    try {
                        builder.type(OGType.from(m.content()));
                    } catch (Throwable e) {
                        log.warn("{}: {}", e.getClass(), e.getLocalizedMessage());
                    }
                    break;
                case Tags.OG_URL:
                    readMetaUrl(m.content(), location).ifPresent(builder::url);
                    break;
                case Tags.OG_IMAGE:
                    readMetaUri(m.content(), location).ifPresent(builder::image);
                    break;
                case Tags.OG_DESCRIPTION:
                    if (m.content() != null && !m.content().isBlank()) {
                        builder.description(m.content());
                    }
                    break;
                case Tags.OG_LOCALE:
                    if (m.content() != null) {
                        builder.locale(Locale.forLanguageTag(m.content().replace('_', '-')));
                    }
                    break;
                default:
            }
        }
        return builder.build();
    }

    private Optional<URL> readMetaUrl(String link, URI location) {
        return readMetaUri(link, location)
                .map(OGScrapperUtils::removeQueryString)
                .flatMap(uri -> {
                    try {
                        return Optional.of(uri.toURL());
                    } catch (MalformedURLException e) {
                        return Optional.empty();
                    }
                });
    }

    private Optional<URI> readMetaUri(String link, URI location) {
        if (link == null || link.isBlank()) {
            return Optional.empty();
        }

        Optional<URI> uri = Optional.of(link).flatMap(u -> {
            try {
                return Optional.of(URI.create(u));
            } catch (Exception e) {
                return Optional.empty();
            }
        });

        if (location != null) {
            return uri.map(location::resolve);
        }

        return uri;
    }
}
