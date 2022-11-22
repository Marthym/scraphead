package fr.ght1pc9kc.scraphead.core.scrap;

import fr.ght1pc9kc.scraphead.core.HeadScraper;
import fr.ght1pc9kc.scraphead.core.http.ScrapClient;
import fr.ght1pc9kc.scraphead.core.http.ScrapRequest;
import fr.ght1pc9kc.scraphead.core.http.ScrapRequestBuilder;
import fr.ght1pc9kc.scraphead.core.model.Metas;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
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
    private static final String BODY_START_TAG = "<body";
    private static final int MAX_HEAD_SIZE = 600_000;
    private static final Pattern CHARSET_EXTRACT = Pattern.compile("<meta.*?charset=[\"']?([^\"']+)");

    private final ScrapClient http;

    private final DocumentMetaReader ogReader;

    @Override
    public Mono<Metas> scrap(URI location) {
        return scrapHead(ScrapRequest.builder(location).build());
    }

    @Override
    public Mono<Metas> scrap(ScrapRequest request) {
        return scrapHead(request);
    }

    private Mono<Metas> scrapHead(ScrapRequest request) {
        try {
            ScrapRequest scrapRequest = request;
            if (request.headers().firstValue("Accept-Charset").isEmpty()) {
                scrapRequest = ScrapRequestBuilder.from(request)
                        .addHeader("Accept-Charset", StandardCharsets.UTF_8.name())
                        .build();
            }
            return http.send(scrapRequest)
                    .flatMap(response -> {
                        AtomicReference<Charset> responseCharset = new AtomicReference<>(OGScrapperUtils.charsetFrom(response.headers()));

                        return response.body()

                                .switchOnFirst(computeCharacterEncoding(request.location(), responseCharset))

                                .scan(new StringBuilder(), (sb, buff) -> {
                                    assert buff.hasArray();
                                    String newContent = new String(buff.array(), responseCharset.get());
                                    return sb.append(newContent);
                                })
                                .takeUntil(sb -> sb.length() >= MAX_HEAD_SIZE
                                        || sb.indexOf(HEAD_END_TAG) >= 0
                                        || sb.indexOf(BODY_START_TAG) >= 0)
                                .last();

                    })
                    .map(StringBuilder::toString)
                    .doFirst(() -> log.trace("Receiving data from {}...", request.location()))

                    .map(html -> {
                        int idxHead = html.indexOf(HEAD_END_TAG);
                        if (idxHead > 0) {
                            return html.substring(0, Math.max(idxHead, idxHead + HEAD_END_TAG.length()));
                        }
                        int idxBody = html.indexOf(BODY_START_TAG);
                        return (idxBody > 0) ? html.substring(0, idxBody) : html;
                    })
                    .map(html -> Jsoup.parseBodyFragment(html, request.location().toString()))
                    .map(ogReader::read)

                    .onErrorResume(e -> {
                        log.warn(WARNING_MESSAGE, e.getLocalizedMessage(), request.location());
                        log.debug(STACKTRACE_DEBUG_MESSAGE, e);
                        return Mono.empty();
                    });
        } catch (Exception e) {
            log.warn(WARNING_MESSAGE, e.getLocalizedMessage(), request.location());
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
