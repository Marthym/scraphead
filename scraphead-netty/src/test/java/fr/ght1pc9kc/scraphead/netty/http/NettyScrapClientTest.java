package fr.ght1pc9kc.scraphead.netty.http;

import fr.ght1pc9kc.scraphead.core.http.ScrapRequest;
import fr.ght1pc9kc.scraphead.core.http.ScrapResponse;
import fr.ght1pc9kc.scraphead.netty.http.config.NettyClientBuilder;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;
import reactor.test.StepVerifier;

import java.net.URI;
import java.net.http.HttpHeaders;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
class NettyScrapClientTest {
    private static DisposableServer mockServer;
    private NettyScrapClient tested;

    @BeforeAll
    static void setUpAll() {
        log.info("Start Server ...");
        mockServer = HttpServer.create()
                .host("localhost")
                .wiretap(true)
                .accessLog(log.isDebugEnabled())
                .route(routes -> routes
                        .get("/test.json", (request, response) ->
                                response.status(200)
                                        .addHeader(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
                                        .sendString(Mono.just("{status: \"FAIL\"}"))
                        )
                        .get("/the-cantina-band.mp3", (request, response) ->
                                response.status(200)
                                        .addHeader(HttpHeaderNames.CONTENT_TYPE, "audio/mpeg3")
                                        .addHeader(HttpHeaderNames.CONTENT_LENGTH, "1200001")
                                        .chunkedTransfer(true)
                                        .send(Flux.create(sink -> {
                                            Random random = new Random();
                                            AtomicInteger totalSize = new AtomicInteger(0);
                                            sink.onRequest(n -> {
                                                int give = 0;
                                                while (totalSize.get() < 1_200_001 && give++ >= n) {
                                                    byte[] buff = new byte[1024];
                                                    random.nextBytes(buff);
                                                    totalSize.accumulateAndGet(buff.length, Integer::sum);
                                                }
                                                if (totalSize.get() < 1_200_001) {
                                                    sink.complete();
                                                }
                                            });
                                        }))
                        )
                ).bindNow();
        log.debug("Server started on {} !", mockServer.port());
    }

    @BeforeEach
    void setUp() {
        tested = new NettyScrapClient(NettyClientBuilder.getNettyHttpClient());
    }

    @Test
    void should_send_request_for_non_html() {
        int port = mockServer.port();

        Flux<ByteBuffer> actual = tested.send(new ScrapRequest(
                        URI.create("http://" + mockServer.host() + ":" + port + "/test.json"),
                        HttpHeaders.of(Map.of(), (l, r) -> true), List.of()))
                .flatMapMany(ScrapResponse::body);

        StepVerifier.create(actual).verifyComplete();
    }

    @Test
    void should_send_request_for_heavy_payload() {
        int port = mockServer.port();

        Flux<ByteBuffer> actual = tested.send(new ScrapRequest(
                        URI.create("http://localhost:" + port + "/the-cantina-band.mp3"),
                        HttpHeaders.of(Map.of(), (l, r) -> true), List.of()))
                .flatMapMany(ScrapResponse::body);

        StepVerifier.create(actual).verifyComplete();
    }

    @AfterAll
    static void tearDown() {
        mockServer.disposeNow(Duration.ofSeconds(2));
    }
}