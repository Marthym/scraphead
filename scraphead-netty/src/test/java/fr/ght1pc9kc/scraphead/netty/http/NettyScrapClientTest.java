package fr.ght1pc9kc.scraphead.netty.http;

import fr.ght1pc9kc.scraphead.core.http.ScrapRequest;
import fr.ght1pc9kc.scraphead.core.http.ScrapResponse;
import fr.ght1pc9kc.scraphead.netty.http.config.NettyClientBuilder;
import io.netty.handler.codec.http.HttpHeaderNames;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.configuration.ConfigurationProperties;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.matchers.Times;
import org.mockserver.model.Header;
import org.mockserver.model.MediaType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpHeaders;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

class NettyScrapClientTest {
    private static ClientAndServer mockServer;

    private NettyScrapClient tested;

    @BeforeAll
    static void setUpAll() {
        ConfigurationProperties.logLevel("WARN");
        mockServer = ClientAndServer.startClientAndServer();
        mockServer.when(request()
                        .withMethod("GET")
                        .withPath("/og-head-test.html"), Times.exactly(1))
                .respond(response()
                        .withStatusCode(200)
                        .withBody(getBodyFromResource("og-head-test.html")));
        mockServer.when(request()
                        .withMethod("GET")
                        .withPath("/og-nohead-test.html"), Times.exactly(1))
                .respond(response()
                        .withStatusCode(200)
                        .withBody(getBodyFromResource("og-nohead-test.html")));
        mockServer.when(request()
                        .withMethod("GET")
                        .withPath("/test.json"), Times.exactly(1))
                .respond(response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON_UTF_8)
                        .withBody("FAIL"));
        mockServer.when(request()
                        .withMethod("GET")
                        .withPath("/the-cantina-band.mp3"), Times.exactly(1))
                .respond(response()
                        .withStatusCode(200)
                        .withHeaders(
                                new Header(HttpHeaderNames.CONTENT_LENGTH.toString(), "1200001"))
                        .withBody(""));
    }

    private static String getBodyFromResource(String file) {
        try (InputStream ras = NettyScrapClientTest.class.getClassLoader().getResourceAsStream(file)) {
            return IOUtils.toString(Objects.requireNonNull(ras), StandardCharsets.UTF_8);
        } catch (IOException e) {
            Assertions.fail("Unable to get resource", e);
            throw new RuntimeException(e);
        }
    }

    @BeforeEach
    void setUp() {
        tested = new NettyScrapClient(NettyClientBuilder.getNettyHttpClient());
    }

    @Test
    void should_send_request_for_non_html() {
        Integer port = mockServer.getLocalPort();

        Flux<ByteBuffer> actual = tested.send(new ScrapRequest(
                        URI.create("http://localhost:" + port + "/test.json"),
                        HttpHeaders.of(Map.of(), (l, r) -> true), List.of()))
                .flatMapMany(ScrapResponse::body);

        StepVerifier.create(actual).verifyComplete();
    }

    @Test
    void should_send_request_for_heavy_payload() {
        Integer port = mockServer.getLocalPort();

        Flux<ByteBuffer> actual = tested.send(new ScrapRequest(
                        URI.create("http://localhost:" + port + "/the-cantina-band.mp3"),
                        HttpHeaders.of(Map.of(), (l, r) -> true), List.of()))
                .flatMapMany(ScrapResponse::body);

        StepVerifier.create(actual).verifyComplete();
    }

    @AfterAll
    static void tearDown() {
        mockServer.stop();
    }
}