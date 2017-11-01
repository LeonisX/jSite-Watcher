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
        html = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">";
        //html = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">";
        //html = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">";
        html = "<!DOCTYPE html><html><head></head></html>";
        //html = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Frameset//EN\" \"http://www.w3.org/TR/html4/frameset.dtd\">";
        //html = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">";
        log.warn("Result: " + HtmlParser.parse(html.getBytes()));
    }
}
