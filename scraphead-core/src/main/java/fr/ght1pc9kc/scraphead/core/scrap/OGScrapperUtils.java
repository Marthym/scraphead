package fr.ght1pc9kc.scraphead.core.scrap;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.URL;
import java.net.http.HttpHeaders;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Slf4j
@UtilityClass
public class OGScrapperUtils {
    public static final String META_CONTENT = "content";
    public static final String META_HREF = "href";
    public static final String META_NAME = "name";
    public static final String META_PROPERTY = "property";
    public static final String META_REL = "rel";
    public static final String META_TYPE = "type";

    public static String removeQueryString(String uri) {
        int idx = uri.indexOf('?');
        return (idx < 0) ? uri : uri.substring(0, idx);
    }

    public static URI removeQueryString(URI uri) {
        if (uri.getQuery() == null) {
            return uri;
        } else {
            return URI.create(removeQueryString(uri.toString()));
        }
    }

    public static Optional<URL> toUrl(String link) {
        return toUri(link).flatMap(u -> {
            try {
                return Optional.of(u.toURL());
            } catch (Exception e) {
                return Optional.empty();
            }
        });
    }

    public static Optional<URI> toUri(String link) {
        if (link == null || link.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(URI.create(link));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static Charset charsetFrom(HttpHeaders headers) {
        if (headers == null) {
            return StandardCharsets.UTF_8;
        }
        String type = headers.firstValue("Content-type")
                .orElse("text/html; charset=utf-8");
        int i = type.indexOf(";");
        if (i >= 0) type = type.substring(i + 1);
        try {
            int eqIdx = type.indexOf('=');
            String value = (eqIdx >= 0) ? type.substring(eqIdx + 1) : null;
            if (value == null) return StandardCharsets.UTF_8;
            return Charset.forName(value);
        } catch (Exception x) {
            log.trace("Can't find charset in {} ({})", type, x.getLocalizedMessage(), x);
            return StandardCharsets.UTF_8;
        }
    }

}
