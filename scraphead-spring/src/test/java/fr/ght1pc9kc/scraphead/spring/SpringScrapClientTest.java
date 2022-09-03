package fr.ght1pc9kc.scraphead.spring;

import fr.ght1pc9kc.scraphead.core.http.ScrapRequest;
import fr.ght1pc9kc.scraphead.core.http.ScrapResponse;
import fr.ght1pc9kc.scraphead.core.model.ex.UnsupportedContentTypeException;
import fr.ght1pc9kc.scraphead.spring.config.ScrapheadWebClientConfiguration;
import io.netty.handler.codec.http.HttpHeaderNames;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockserver.configuration.ConfigurationProperties;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.matchers.Times;
import org.mockserver.model.Header;
import org.mockserver.model.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
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
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

class SpringScrapClientTest {
    private static ClientAndServer mockServer;

    private SpringScrapClient tested;

    @BeforeAll
    static void setUpAll() {
        ConfigurationProperties.logLevel("WARN");
        mockServer = ClientAndServer.startClientAndServer();
        mockServer.when(request()
                        .withMethod("GET")
                        .withPath("/invalid-content-type.html"), Times.exactly(1))
                .respond(response()
                        .withStatusCode(200)
                        .withHeader(CONTENT_TYPE, "text/html, charset=iso-8859-1")
                        .withBody(getBodyFromResource("og-head-test.html")));

        mockServer.when(request()
                        .withMethod("GET")
                        .withPath("/bad-content-type.html"), Times.exactly(1))
                .respond(response()
                        .withStatusCode(200)
                        .withHeader(CONTENT_TYPE, "text/html: charset=iso-8859-1")
                        .withBody(getBodyFromResource("og-head-test.html")));

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
                                new Header(HttpHeaderNames.CONTENT_LENGTH.toString(), Integer.toString(3 * 1024 * 1024)))
                        .withBody(RandomUtils.nextBytes(3 * 1024 * 1024)));
    }

    private static String getBodyFromResource(String file) {
        try (InputStream ras = SpringScrapClientTest.class.getClassLoader().getResourceAsStream(file)) {
            return IOUtils.toString(Objects.requireNonNull(ras), StandardCharsets.UTF_8);
        } catch (IOException e) {
            Assertions.fail("Unable to get resource", e);
            throw new RuntimeException(e);
        }
    }

    @BeforeEach
    void setUp() {
        WebClient webClient = new ScrapheadWebClientConfiguration().scrapheadWebclient();
        tested = new SpringScrapClient(webClient);
    }

    @ParameterizedTest
    @CsvSource({
            "/og-head-test.html",
            "/og-nohead-test.html",
            "/invalid-content-type.html",
            "/bad-content-type.html",
    })
    void should_send_request(String path) {
        Integer port = mockServer.getLocalPort();

        Flux<ByteBuffer> actual = tested.send(new ScrapRequest(
                        URI.create("http://localhost:" + port + path),
                        HttpHeaders.of(Map.of(), (l, r) -> true), List.of()))
                .flatMapMany(ScrapResponse::body);

        StepVerifier.create(actual)
                .expectNextMatches(bb -> new String(bb.array(), StandardCharsets.UTF_8)
                        .endsWith("<link rel=\"stylesheet\" href=\"https://blog.i-run.si/css/bundle.min.css\">\n"))
                .verifyComplete();
    }

    @Test
    void should_send_request_for_non_html() {
        Integer port = mockServer.getLocalPort();

        Flux<ByteBuffer> actual = tested.send(new ScrapRequest(
                        URI.create("http://localhost:" + port + "/test.json"),
                        HttpHeaders.of(Map.of(), (l, r) -> true), List.of()))
                .flatMapMany(ScrapResponse::body);

        StepVerifier.create(actual)
                .verifyError(UnsupportedContentTypeException.class);
    }

    @Test
    void should_send_request_for_heavy_payload() {
        Integer port = mockServer.getLocalPort();

        Flux<ByteBuffer> actual = tested.send(new ScrapRequest(
                        URI.create("http://localhost:" + port + "/the-cantina-band.mp3"),
                        HttpHeaders.of(Map.of(), (l, r) -> true), List.of()))
                .flatMapMany(ScrapResponse::body);

        StepVerifier.create(actual)
                .verifyError(IllegalStateException.class);
    }

    @AfterAll
    static void tearDown() {
        mockServer.stop();
    }
}