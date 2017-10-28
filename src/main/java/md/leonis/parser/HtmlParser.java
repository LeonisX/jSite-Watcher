package md.leonis.parser;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import md.leonis.parser.domain.PageEncoding;
import md.leonis.parser.domain.State;

@Slf4j
@Getter
@Setter
public class HtmlParser {

    private PageEncoding pageEncoding;
    private byte[] html;
    private String htmlString;
    private int position = 0;

    private State state = State.DATA;

    public static String parse(byte[] html) {
        return parse(html, null);
    }

    public static String parse(byte[] html, String encoding) {
        Parser parser = new Parser(html, encoding);
        return parser.parse();
    }

}
