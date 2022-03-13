module scraphead.core {
    requires static lombok;
    requires static org.jetbrains.annotations;
    requires reactor.core;
    requires org.reactivestreams;
    requires java.net.http;
    requires org.slf4j;
    requires java.desktop;
    requires org.jsoup;

    exports fr.ght1pc9kc.scraphead.core;
    exports fr.ght1pc9kc.scraphead.core.http;
    exports fr.ght1pc9kc.scraphead.core.model;
    exports fr.ght1pc9kc.scraphead.core.model.opengraph;
    exports fr.ght1pc9kc.scraphead.core.model.twitter;
    exports fr.ght1pc9kc.scraphead.core.model.links;
}