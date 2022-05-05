module scraphead.netty {
    requires static lombok;

    requires io.netty.buffer;
    requires io.netty.codec.http;
    requires io.netty.codec;
    requires io.netty.transport;
    requires java.net.http;
    requires org.reactivestreams;
    requires org.slf4j;
    requires reactor.core;
    requires reactor.netty.core;
    requires reactor.netty.http;
    requires reactor.netty;
    requires scraphead.core;

    exports fr.ght1pc9kc.scraphead.netty.http;
    exports fr.ght1pc9kc.scraphead.netty.http.config;
}