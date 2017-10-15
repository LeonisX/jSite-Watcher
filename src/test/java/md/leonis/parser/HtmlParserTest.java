package md.leonis.parser;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class HtmlParserTest {

    @Test(expected = RuntimeException.class)
    public void parseWithoutEncoding() throws Exception {
        assertThat(HtmlParser.parse("".getBytes()), is(""));
    }

    @Test
    public void parseWithEncoding() throws Exception {
        assertThat(HtmlParser.parse("".getBytes(), ""), is(""));
    }
}