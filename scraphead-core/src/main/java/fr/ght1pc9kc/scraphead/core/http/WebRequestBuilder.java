package fr.ght1pc9kc.scraphead.core.http;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.net.HttpCookie;
import java.net.URI;
import java.net.http.HttpHeaders;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiPredicate;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class WebRequestBuilder {
    private static final Set<String> DISALLOWED_HEADERS_SET = getDisallowedHeaders();
    private static final BiPredicate<String, String>
            ALLOWED_HEADERS = (header, unused) -> !DISALLOWED_HEADERS_SET.contains(header);

    private final URI location;

    private final Map<String, List<String>> headers = new HashMap<>();
    private final List<HttpCookie> cookies = new ArrayList<>();

    private WebRequestBuilder(WebRequest copy) {
        this.location = copy.location();
        this.headers.putAll(copy.headers().map());
        this.cookies.addAll(copy.cookies());
    }

    public static WebRequestBuilder from(WebRequest copy) {
        return new WebRequestBuilder(copy);
    }

    private static Set<String> getDisallowedHeaders() {
        Set<String> headers = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        headers.addAll(Set.of("connection", "content-length", "expect", "host", "upgrade"));
        return Collections.unmodifiableSet(headers);
    }

    public WebRequestBuilder addHeader(String name, String value) {
        headers.compute(name, (k, v) -> {
            List<String> newValue = v;
            if (newValue == null) {
                newValue = new ArrayList<>();
            }
            newValue.add(value);
            return newValue;
        });
        return this;
    }

    public WebRequestBuilder addCookie(String name, String value) {
        cookies.add(new HttpCookie(name, value));
        return this;
    }

    public WebRequest build() {
        return new WebRequest(location, HttpHeaders.of(headers, ALLOWED_HEADERS), cookies);
    }
}
