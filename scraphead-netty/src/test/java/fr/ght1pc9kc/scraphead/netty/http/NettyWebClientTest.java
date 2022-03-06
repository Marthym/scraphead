package fr.ght1pc9kc.scraphead.netty.http;

import fr.ght1pc9kc.scraphead.core.http.WebRequest;
import fr.ght1pc9kc.scraphead.core.http.WebResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import org.apache.commons.io.IOUtils;
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

class NettyWebClientTest {
    private static ClientAndServer mockServer;

    private NettyWebClient tested;

    @BeforeAll
    static void setUpAll() throws IOException {
        ConfigurationProperties.logLevel("WARN");
        InputStream ras = NettyWebClientTest.class.getClassLoader().getResourceAsStream("og-head-test.html");
        String body = IOUtils.toString(Objects.requireNonNull(ras), StandardCharsets.UTF_8);
        mockServer = ClientAndServer.startClientAndServer();
        mockServer.when(request()
                        .withMethod("GET")
                        .withPath("/og-head-test.html"), Times.exactly(1))
                .respond(response()
                        .withStatusCode(200)
                        .withBody(body));
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

    @BeforeEach
    void setUp() {
        tested = new NettyWebClient();
    }

    @Test
    void should_send_request() {
        Integer port = mockServer.getLocalPort();

        Flux<ByteBuffer> actual = tested.send(new WebRequest(
                        URI.create("http://localhost:" + port + "/og-head-test.html"),
                        HttpHeaders.of(Map.of(), (l, r) -> true), List.of()))
                .flatMapMany(WebResponse::body);

        StepVerifier.create(actual)
                .expectNextMatches(bb -> new String(bb.array(), StandardCharsets.UTF_8)
                        .endsWith("<link rel=\"stylesheet\" href=\"https://blog.i-run.si/css/bundle.min.css\">\n"))
                .verifyComplete();
    }

    @Test
    void should_send_request_for_non_html() {
        Integer port = mockServer.getLocalPort();

        Flux<ByteBuffer> actual = tested.send(new WebRequest(
                        URI.create("http://localhost:" + port + "/test.json"),
                        HttpHeaders.of(Map.of(), (l, r) -> true), List.of()))
                .flatMapMany(WebResponse::body);

        StepVerifier.create(actual).verifyComplete();
    }

    @Test
    void should_send_request_for_heavy_payload() {
        Integer port = mockServer.getLocalPort();

        Flux<ByteBuffer> actual = tested.send(new WebRequest(
                        URI.create("http://localhost:" + port + "/the-cantina-band.mp3"),
                        HttpHeaders.of(Map.of(), (l, r) -> true), List.of()))
                .flatMapMany(WebResponse::body);

        StepVerifier.create(actual).verifyComplete();
    }

    @AfterAll
    static void tearDown() {
        mockServer.stop();
    }
}