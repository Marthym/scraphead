package fr.ght1pc9kc.scraphead.netty.http.config;

import lombok.experimental.UtilityClass;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.SslProvider;

import java.util.Set;

@UtilityClass
public class NettyClientBuilder {

    public static HttpClient getNettyHttpClient() {
        return HttpClient.create()
                .secure(spec -> spec.sslContext(SslProvider.defaultClientProvider().getSslContext()))
                .followRedirect((req, res) -> // 303 was not in the default code
                        Set.of(301, 302, 303, 307, 308).contains(res.status().code()))
                .compress(true);
    }
}
