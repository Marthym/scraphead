module scraphead.netty {
    requires static lombok;

    requires scraphead.core;
    requires org.slf4j;
    requires org.reactivestreams;
    requires reactor.core;
    requires reactor.netty;
    requires reactor.netty.http;
    requires reactor.netty.core;
    requires java.net.http;
    requires io.netty.buffer;
    requires io.netty.transport;
    requires io.netty.codec;
    requires io.netty.codec.http;
}