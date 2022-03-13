package fr.ght1pc9kc.scraphead.core.scrap;

import fr.ght1pc9kc.scraphead.core.scrap.model.Meta;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.stream.Stream;

class OGScrapperUtilsTest {
    @ParameterizedTest
    @CsvSource({
            "https://www.jedi.com/obiwan/?q=jedi, https://www.jedi.com/obiwan/",
            "https://www.jedi.com/obiwan?q=jedi, https://www.jedi.com/obiwan",
            "https://www.jedi.com/?obiwan/?q=jedi, https://www.jedi.com/",
    })
    void should_remove_querystring_from_uri(String uri, String expected) {
        URI actual = OGScrapperUtils.removeQueryString(URI.create(uri));
        Assertions.assertThat(actual).hasToString(expected);
    }

    @ParameterizedTest
    @CsvSource({
            "https://www.jedi.com/obiwan/?q=jedi, https://www.jedi.com/obiwan/",
            "https://www.jedi.com/obiwan?q=jedi, https://www.jedi.com/obiwan",
            "https://www.jedi.com/?obiwan/?q=jedi, https://www.jedi.com/",
            "www.jedi.com/?obiwan/?q=jedi, www.jedi.com/",
    })
    void should_remove_querystring_from_string(String uri, String expected) {
        String actual = OGScrapperUtils.removeQueryString(uri);
        Assertions.assertThat(actual).isEqualTo(expected);
    }

}