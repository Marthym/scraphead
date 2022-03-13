package fr.ght1pc9kc.scraphead.core.scrap;

import fr.ght1pc9kc.scraphead.core.scrap.model.Node;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.IntSupplier;

@UtilityClass
public class HtmlFragmentParser {
    private static final Set<String> KEEPING_TAG = Set.of("html", "meta", "link");

    public static List<Node> parse(String html) {
        List<Node> nodes = new ArrayList<>();

        int i = -1;
        while (i < html.length()) {
            ++i;
            if (isTagOpening(html, i)) {
                int tagEndIdx = minIndex(html.length(),
                        html.indexOf(' ', i),
                        html.indexOf('>', i));

                String currentTag = html.substring(i + 1, tagEndIdx);
                if (!KEEPING_TAG.contains(currentTag)) {
                    continue;
                }
                if (html.charAt(tagEndIdx) != '>') {
                    State<Map<String, String>> state = parseAttributes(html, tagEndIdx);
                    nodes.add(new Node(currentTag, state.data()));
                    i = state.idx();
                } else {
                    nodes.add(new Node(currentTag, Map.of()));
                    i = tagEndIdx;
                }

            }
        }

        return List.copyOf(nodes);
    }

    private static State<Map<String, String>> parseAttributes(String html, int start) {
        int idx = start;
        String key = null;
        Map<String, String> attributes = new HashMap<>();
        int htmlLength = html.length();

        int i = start - 1;
        while (i < htmlLength) {
            boolean hasKeySet = (key != null);
            ++i;
            if (isClosingTag(html, i)) {
                if (hasKeySet) {
                    attributes.put(key, "");
                }
                if (isAutoclosing(html, i)) {
                    ++i;
                }
                return new State<>(i, Map.copyOf(attributes));

            } else if (isStartingKey(!hasKeySet, html, i)) {
                int keyEndIdx = minIndex(htmlLength,
                        html.indexOf('=', i),
                        html.indexOf('>', i),
                        html.indexOf(' ', i)
                );
                key = html.substring(i, keyEndIdx).trim();
                i = keyEndIdx - 1;
                continue;

            } else if (isStartingValue(hasKeySet, html, idx)) {
                int j = i;
                IntSupplier tagMaxEnd = () -> minIndex(htmlLength, html.indexOf('>', j));
                IntSupplier tagMaxAutoClose = () -> minIndex(tagMaxEnd, html.indexOf("/>", j));
                int startIndex = minIndex(tagMaxAutoClose,
                        html.indexOf('\'', i),
                        html.indexOf('"', i));
                int endIndex = html.indexOf(html.charAt(startIndex), startIndex + 1);
                String value = html.substring(startIndex + 1, endIndex);
                i = endIndex - 1;
                attributes.put(key, value);
                key = null;

            } else if (hasKeySet && Character.isAlphabetic(html.charAt(i))) {
                attributes.put(key, "");
                key = null;
                --i;
            }
            idx = i;
        }

        return new State<>(idx, Map.copyOf(attributes));
    }

    private static int minIndex(IntSupplier defaultMax, int... idxs) {
        return Arrays.stream(idxs)
                .filter(i -> i >= 0)
                .min()
                .orElseGet(defaultMax);
    }

    private static int minIndex(int defaultMax, int... idxs) {
        return minIndex(() -> defaultMax, idxs);
    }

    private static boolean isAutoclosing(String html, int idx) {
        return html.charAt(idx) == '/' && html.charAt(idx + 1) == '>';
    }

    private static boolean isClosingTag(String html, int idx) {
        return html.charAt(idx) == '>' || isAutoclosing(html, idx);
    }

    private static boolean isTagOpening(String html, int idx) {
        return html.length() > idx + 1
                && html.charAt(idx) == '<' && html.charAt(idx + 1) != '/';
    }

    private static boolean isQuote(String html, int idx) {
        return html.charAt(idx) == '"' || html.charAt(idx) == '\'';
    }

    private static boolean isQuoteOrSpace(String html, int idx) {
        return isQuote(html, idx) || html.charAt(idx) == ' ';
    }

    private static boolean isStartingKey(boolean asKey, String html, int idx) {
        return asKey && !isQuoteOrSpace(html, idx);
    }

    private static boolean isStartingValue(boolean asKey, String html, int idx) {
        return asKey && html.charAt(idx) == '=';
    }

    private record State<T>(
            int idx,
            T data
    ) {
    }
}
