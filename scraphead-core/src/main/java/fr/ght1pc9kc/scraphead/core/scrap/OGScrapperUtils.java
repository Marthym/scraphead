package fr.ght1pc9kc.scraphead.core.scrap;

import fr.ght1pc9kc.scraphead.core.scrap.model.Meta;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.http.HttpHeaders;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@UtilityClass
public class OGScrapperUtils {
    private static final String META_PROPERTY = "property";
    private static final String META_NAME = "name";
    private static final String META_CONTENT = "content";

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
            log.trace("Can't find charset in {} ({})", type, x);
            return StandardCharsets.UTF_8;
        }
    }

    public static List<Meta> extractMetaHeaders(String html) {
        return HtmlFragmentParser.parse(html).stream()
                .filter(n -> "meta".equals(n.tag()) && (n.attr().containsKey(META_PROPERTY) || n.attr().containsKey(META_NAME)))
                .map(n -> new Meta(n.attr().getOrDefault(META_PROPERTY, n.attr().get(META_NAME)), n.attr().get(META_CONTENT)))
                .toList();
    }
}
