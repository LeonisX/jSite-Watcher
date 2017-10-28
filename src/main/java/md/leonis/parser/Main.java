package md.leonis.parser;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {

    public static void main(String[] args) {
        String html = "fgh <!-- asd --> e <meta > 1  <!  /> ddd <meta tra ta ta";
        //log.warn("Result: " + HtmlParser.parse(html.getBytes()));

        html = "<html><meta charset='utf-8' /> </html>";
        //log.warn("Result: " + HtmlParser.parse(html.getBytes()));

        html = "<meta http-equiv=\"content-type\" content=\"text/html; charset=windows-1251\" />";
        //log.warn("Result: " + HtmlParser.parse(html.getBytes()));

        html = "<meta content=\"text/html; charset=windows-1251\" http-equiv=\"content-type\" /><p>asd</p>";
        html = "<p attr=asd e=2>asd</p>";
        log.warn("Result: " + HtmlParser.parse(html.getBytes()));
    }
}
