module scraphead.spring {
    requires static lombok;

    requires java.net.http;
    requires org.reactivestreams;
    requires org.slf4j;
    requires reactor.core;
    requires reactor.netty.core;
    requires reactor.netty.http;
    requires reactor.netty;
    requires scraphead.core;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires spring.core;
    requires spring.web;
    requires spring.webflux;
}