package fr.ght1pc9kc.scraphead.core.scrap.collectors;

import fr.ght1pc9kc.scraphead.core.model.opengraph.OGType;
import fr.ght1pc9kc.scraphead.core.model.opengraph.OpenGraph;
import fr.ght1pc9kc.scraphead.core.scrap.MetaDataCollector;
import fr.ght1pc9kc.scraphead.core.scrap.OGScrapperUtils;
import fr.ght1pc9kc.scraphead.core.scrap.model.MetaType;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Element;

import java.util.Locale;

import static fr.ght1pc9kc.scraphead.core.scrap.OGScrapperUtils.META_CONTENT;
import static fr.ght1pc9kc.scraphead.core.scrap.OGScrapperUtils.META_NAME;
import static fr.ght1pc9kc.scraphead.core.scrap.OGScrapperUtils.META_PROPERTY;

@Slf4j
public class OpenGraphCollector implements MetaDataCollector {
    private static final String OG_NAMESPACE = "og:";
    private static final String OG_TITLE = OG_NAMESPACE + "title";
    private static final String OG_TYPE = OG_NAMESPACE + "type";
    private static final String OG_URL = OG_NAMESPACE + "url";
    private static final String OG_IMAGE = OG_NAMESPACE + "image";
    private static final String OG_DESCRIPTION = OG_NAMESPACE + "description";
    private static final String OG_LOCALE = OG_NAMESPACE + "locale";

    private final OpenGraph.OpenGraphBuilder builder;

    public OpenGraphCollector() {
        builder = OpenGraph.builder();
    }

    @Override
    public MetaType type() {
        return MetaType.OPENGRAPH;
    }

    @Override
    public void inspect(Element element) {
        if (!"meta".equals(element.tagName())) {
            return;
        }
        String property = (element.hasAttr(META_PROPERTY))
                ? element.attr(META_PROPERTY) : element.attr(META_NAME);

        switch (property) {
            case OG_TITLE:
                builder.title(element.attr(META_CONTENT));
                break;
            case OG_TYPE:
                try {
                    builder.type(OGType.from(element.attr(META_CONTENT)));
                } catch (Exception e) {
                    log.warn("{}: {}", e.getClass(), e.getLocalizedMessage());
                }
                break;
            case OG_URL:
                OGScrapperUtils.toUrl(element.attr("abs:" + META_CONTENT))
                        .ifPresent(builder::url);
                break;
            case OG_IMAGE:
                OGScrapperUtils.toUri(element.attr("abs:" + META_CONTENT))
                        .ifPresent(builder::image);
                break;
            case OG_DESCRIPTION:
                if (element.hasAttr(META_CONTENT)) {
                    builder.description(element.attr(META_CONTENT));
                }
                break;
            case OG_LOCALE:
                if (element.hasAttr(META_CONTENT)) {
                    builder.locale(Locale.forLanguageTag(
                            element.attr(META_CONTENT).replace('_', '-')));
                }
                break;
            default:
        }
    }

    @Override
    public <T> T collect(Class<T> clazz) {
        if (OpenGraph.class.equals(clazz)) {
            OpenGraph og = builder.build();
            return clazz.cast(og);
        }
        throw new IllegalArgumentException("Unable to collect " + clazz.getSimpleName());
    }
}
