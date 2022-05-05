package fr.ght1pc9kc.scraphead.netty.http.config;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import lombok.experimental.UtilityClass;
import reactor.netty.http.Http11SslContextSpec;
import reactor.netty.http.client.HttpClient;

import java.nio.charset.StandardCharsets;
import java.util.Set;

@UtilityClass
public class NettyClientBuilder {
    private static final int MAX_FRAME_LENGTH = 600_000;
    private static final ByteBuf FRAME_HEAD_DELIMITER = Unpooled.wrappedBuffer("</head>".getBytes(StandardCharsets.UTF_8));
    private static final ByteBuf FRAME_BODY_DELIMITER = Unpooled.wrappedBuffer("<body".getBytes(StandardCharsets.UTF_8));

    public static HttpClient getNettyHttpClient() {
        return HttpClient.create()
                .doOnConnected(c -> c.addHandler(new DelimiterBasedFrameDecoder(MAX_FRAME_LENGTH, FRAME_HEAD_DELIMITER, FRAME_BODY_DELIMITER))) //FIXME: when reactor-netty {@link HttpOperations} will implement addHandlerLast
                .secure(spec -> spec.sslContext(Http11SslContextSpec.forClient()))
                .followRedirect((req, res) -> // 303 was not in the default code
                        Set.of(301, 302, 303, 307, 308).contains(res.status().code()))
                .compress(true);
    }
}
