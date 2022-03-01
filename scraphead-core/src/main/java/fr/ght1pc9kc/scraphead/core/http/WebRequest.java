package fr.ght1pc9kc.scraphead.core.http;

import java.net.HttpCookie;
import java.net.URI;
import java.net.http.HttpHeaders;
import java.util.List;

public record WebRequest(
        URI location,
        HttpHeaders headers,
        List<HttpCookie> cookies
) {
}
