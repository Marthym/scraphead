package fr.ght1pc9kc.scraphead.core.scrap;

import fr.ght1pc9kc.scraphead.core.model.Meta;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.http.HttpHeaders;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
        String type = headers.firstValue("Content-type")
                .orElse("text/html; charset=utf-8");
        int i = type.indexOf(";");
        if (i >= 0) type = type.substring(i + 1);
        try {
            int eqIdx = type.indexOf('=');
            String value = (eqIdx >= 0) ? type.substring(eqIdx + 1) : null;
            if (value == null) return StandardCharsets.UTF_8;
            return Charset.forName(value);
        } catch (Throwable x) {
            log.trace("Can't find charset in {} ({})", type, x);
            return StandardCharsets.UTF_8;
        }
    }

    public static List<Meta> extractMetaHeaders(String html) {
        List<Meta> metas = new ArrayList<>();
        for (String head : html.split("(<meta )")) {
            try {
                char[] chars = head.toCharArray();
                int nextIdx = 0;
                String metaProperty = null;
                String metaValue = null;

                while ((metaValue == null || metaProperty == null) || nextIdx < head.length()) {
                    int eqIdx = head.indexOf('=', nextIdx);
                    if (eqIdx < 0) {
                        break;
                    }
                    String prop = head.substring(nextIdx, eqIdx).trim();
                    StringBuilder valueBld = new StringBuilder(head.length() - eqIdx);
                    char q = 0;
                    nextIdx = eqIdx + 1;
                    for (int i = nextIdx; i < chars.length; i++) {
                        if ((q != '\'' && q != '"') && (chars[i] == '\'' || chars[i] == '"')) {
                            q = chars[i];
                            continue;
                        } else if ((q == '\'' || q == '"') && (chars[i] == q)) {
                            nextIdx = i + 1;
                            break;
                        }
                        valueBld.append(chars[i]);
                    }
                    String value = valueBld.toString();

                    switch (prop) {
                        case META_NAME:
                            if (metaProperty == null) {
                                metaProperty = value;
                            }
                            break;
                        case META_PROPERTY:
                            metaProperty = value;
                            break;
                        case META_CONTENT:
                            metaValue = value;
                            break;
                    }
                }

                if (metaProperty == null || metaValue == null) {
                    continue;
                }
                metas.add(new Meta(metaProperty, metaValue));
            } catch (Exception e) {
                log.debug("Fail to parse meta {}", head);
                log.debug("STACKTRACE", e);
            }
        }
        return List.copyOf(metas);
    }
}
