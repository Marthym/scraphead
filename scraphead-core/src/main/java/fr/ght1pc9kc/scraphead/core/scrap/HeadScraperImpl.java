package fr.ght1pc9kc.scraphead.core.scrap;

import fr.ght1pc9kc.scraphead.core.HeadScraper;
import fr.ght1pc9kc.scraphead.core.http.WebClient;
import fr.ght1pc9kc.scraphead.core.http.WebRequest;
import fr.ght1pc9kc.scraphead.core.model.Metas;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;

import java.net.HttpCookie;
import java.net.URI;
import java.net.http.HttpHeaders;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@RequiredArgsConstructor
public final class HeadScraperImpl implements HeadScraper {
    public static final String WARNING_MESSAGE = "{} on {}";
    public static final String STACKTRACE_DEBUG_MESSAGE = "STACKTRACE";

    private static final String HEAD_END_TAG = "</head>";
    private static final Pattern CHARSET_EXTRACT = Pattern.compile("<meta.*?charset=[\"']?([^\"']+)");

    private final WebClient http;

    private final DocumentMetaReader ogReader;

    @Override
    public Mono<Metas> scrap(URI location) {
        return scrapHead(location, HttpHeaders.of(Map.of(), (n, v) -> true), List.of());
    }

    @Override
    public Mono<Metas> scrap(URI location, HttpHeaders headers, List<HttpCookie> cookies) {
        return scrapHead(location, headers, cookies);
    }

    private Mono<Metas> scrapHead(URI location, HttpHeaders headers, List<HttpCookie> cookies) {
        try {
            Map<String, List<String>> requestHeaders = new HashMap<>(headers.map());
            requestHeaders.computeIfAbsent("Accept-Charset", k -> new ArrayList<>()).add(StandardCharsets.UTF_8.name());

            WebRequest request = new WebRequest(location, HttpHeaders.of(requestHeaders, (n, v) -> true), cookies);
            return http.send(request)
                    .flatMap(response -> {
                        AtomicReference<Charset> responseCharset = new AtomicReference<>(OGScrapperUtils.charsetFrom(response.headers()));

                        return response.body()

                                .switchOnFirst(computeCharacterEncoding(location, responseCharset))

                                .scan(new StringBuilder(), (sb, buff) -> {
                                    assert buff.hasArray();
                                    String newContent = new String(buff.array(), responseCharset.get());
                                    return sb.append(newContent);
                                })
                                .takeUntil(sb -> sb.indexOf(HEAD_END_TAG) >= 0)
                                .last();

                    })
                    .map(StringBuilder::toString)
                    .doFirst(() -> log.trace("Receiving data from {}...", location))

                    .map(html -> Jsoup.parseBodyFragment(html, location.toString()))
                    .map(ogReader::read)

                    .onErrorResume(e -> {
                        log.warn(WARNING_MESSAGE, e.getLocalizedMessage(), location);
                        log.debug(STACKTRACE_DEBUG_MESSAGE, e);
                        return Mono.empty();
                    });
        } catch (Exception e) {
            log.warn(WARNING_MESSAGE, e.getLocalizedMessage(), location);
            log.debug(STACKTRACE_DEBUG_MESSAGE, e);
            return Mono.empty();
        }
    }

    @NotNull
    private BiFunction<Signal<? extends ByteBuffer>, Flux<ByteBuffer>, Publisher<? extends ByteBuffer>>
    computeCharacterEncoding(URI location, AtomicReference<Charset> responseCharset) {
        CharsetDecoder charsetDecoder = responseCharset.get().newDecoder();
        return (signal, fBuff) -> {
            if (signal.hasValue()) {
                ByteBuffer byteBuffer = signal.get();
                assert byteBuffer != null;
                try {
                    charsetDecoder.decode(byteBuffer);
                } catch (CharacterCodingException e) {
                    try {
                        Matcher m = CHARSET_EXTRACT.matcher(new String(byteBuffer.array(), responseCharset.get()));
                        if (m.find()) {
                            responseCharset.set(Charset.forName(m.group(1)));
                            return fBuff;
                        }
                    } catch (Exception ex) {
                        log.trace("Unable to parse charset encoding from {}", location);
                        log.trace(STACKTRACE_DEBUG_MESSAGE, e);
                    }
                    return Flux.empty();
                }
            }
            return fBuff;
        };
    }
}
