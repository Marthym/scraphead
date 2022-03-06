module scraphead.core {
    requires static lombok;
    requires static org.jetbrains.annotations;
    requires reactor.core;
    requires org.reactivestreams;
    requires java.net.http;
    requires org.slf4j;
    requires java.desktop;

    exports fr.ght1pc9kc.scraphead.core;
    exports fr.ght1pc9kc.scraphead.core.model;
    exports fr.ght1pc9kc.scraphead.core.http;
}