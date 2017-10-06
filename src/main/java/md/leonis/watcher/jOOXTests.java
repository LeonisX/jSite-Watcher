package md.leonis.watcher;

import static java.lang.System.exit;
import static org.joox.JOOX.$;

import com.gargoylesoftware.htmlunit.StringWebResponse;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HTMLParser;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import javafx.application.Application;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.scene.web.WebEngine;
import javafx.stage.Stage;
import md.leonis.watcher.utils.DocumentWrapper;
import org.apache.xerces.parsers.DOMParser;
import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.Tag;
import org.htmlparser.tags.CompositeTag;
import org.htmlparser.util.ParserException;
import org.joox.Match;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class jOOXTests extends Application {



    private static String myString = "<html><head><title>Test</title></head><body><div><p>dfdf</div></body></html>";

    public static void main(String[] args) throws IOException, SAXException, ParserException {

        //xerces
        DOMParser parser = new DOMParser();
        parser.setFeature("http://apache.org/xml/features/continue-after-fatal-error", true);
        //parser.setFeature("http://xml.org/sax/features/use-entity-resolver2", false);

        //	http://apache.org/xml/features/dom/include-ignorable-whitespace
        ErrorHandler errorHandler = new ErrorHandler() {
            @Override
            public void warning(SAXParseException exception) throws SAXException {
                System.out.println("===");
                System.out.println(exception.getMessage());
            }

            @Override
            public void error(SAXParseException exception) throws SAXException {
                System.out.println("===");
                System.out.println(exception.getMessage());
            }

            @Override
            public void fatalError(SAXParseException exception) throws SAXException {
                System.out.println("===");
                System.out.println(exception.getMessage());
            }
        };

        parser.setErrorHandler(errorHandler);


        InputSource inputSource = new InputSource( new StringReader( myString ) );
        parser.parse(inputSource);
        parser.getDocument();
        DocumentWrapper documentWrapper = DocumentWrapper.wrapDocument(parser.getDocument());
        List<String> tags = documentWrapper.walkDocument();

        System.out.println();

// htmlunit
        byte[] encoded = Files.readAllBytes(Paths.get("/home/leonidstavila/4.html"));
        String html = new String(encoded, "utf-8");
        /*String html2 = String.join("\n", Files.readAllLines(Paths.get("/home/leonidstavila/1.html"),
                        Charset.forName("utf-8")));*/
        URL url = new URL("http://emu-land.net");
        StringWebResponse response = new StringWebResponse(html, url);
        WebClient webClient = new WebClient();
        HtmlPage page = HTMLParser.parseHtml(response, webClient.getCurrentWindow());
        System.out.println(page.getTitleText());


            //final HtmlPage page = webClient.getPage("http://htmlunit.sourceforge.net");

        walk(page);
            //page.getChildren().forEach(e -> System.out.println(e.getNodeName()));
            //page.getBody().getChildElements().forEach(e -> System.out.println(e.getTagName()));


/*        exit(0);


        //String html = String.join("\n", Files.readAllLines(Paths.get("/home/leonidstavila/1.html"),
        //        Charset.defaultCharset()));

        String html = "<p>asasas<p>123</p></p><div><p>dfdf</div>";

        Parser parser2 = Parser.createParser (html, null);
        //Parser parser = new Parser("http://tv-games.ru");
        walk(parser2);*/

        launch();
    }

    private static void walk(DomNode node) {
        System.out.print(node.getNodeName());
        System.out.print(" : " + node.getNodeType());
        System.out.println(" : " + node.getNodeValue());
        node.getChildren().forEach(jOOXTests::walk);
    }

    private static void walk(Parser parser) throws ParserException {
        while(parser.elements().hasMoreNodes()) {
            Node node = parser.elements().nextNode();
            if (node instanceof Tag) {
                System.out.println(((Tag) node).getRawTagName());
                if (node instanceof CompositeTag) {
                    System.out.println("      " + ((CompositeTag) node).getStringText());
                }
            } else {
                System.out.println(node.getText());
            }
            walk(node);
        }
    }

    private static void walk(Node node) {
        if (node.getChildren() != null) {
            while (node.getChildren().elements().hasMoreNodes()) {
                Node childNode = node.getChildren().elements().nextNode();
                if (childNode instanceof Tag) {
                    System.out.println(((Tag) childNode).getRawTagName());
                    System.out.println("      " + ((CompositeTag) childNode).getStringText());
                } else {
                    System.out.println(childNode.getText());
                }
                walk(childNode);
            }
        }
    }


    @Override
    public void start(Stage primaryStage) throws Exception {

        WebEngine webEngine = new WebEngine();
        webEngine.getLoadWorker().stateProperty().addListener((ov, oldState, newState) -> {
            if (newState == State.SUCCEEDED) {
                DocumentWrapper documentWrapper1 = DocumentWrapper.wrapDocument(webEngine.getDocument());
                List<String> tags1 = documentWrapper1.walkDocument();
            }
        });
        webEngine.loadContent(myString);




/*
        WebEngine webEngine = new WebEngine();
        webEngine.getLoadWorker().stateProperty().addListener(
                (ObservableValue<? extends Worker.State> observable,
                 Worker.State oldValue,
                 Worker.State newValue) -> {
                    System.out.println(newValue);
                    if( newValue == Worker.State.SUCCEEDED ) {
                        System.out.println("gata");
                        Document document = webEngine.getDocument();

                        DocumentWrapper documentWrapper = DocumentWrapper.wrapDocument(document);

                        // Wrap the document with the jOOX API
                        Match x1 = $(document);
                        //x1.xp
                        //n(x1);
                    }
                    // Your logic here
                } );
        webEngine.setOnAlert(handler -> {
            System.out.println(handler);
        });
        webEngine.setOnError(handler -> {
            System.out.println(handler);
        });
        webEngine.load("http://tv-games.ru");
        File xmlFile = new File("/home/leonis/1.html");
        //Document document = $(xmlFile).document();

*/

        new Stage().showAndWait();


/*
// This will get all books (wrapped <book/> DOM Elements)
        Match x2 = $(document).find("book");

// This will get all even or odd books
        Match x3 = $(document).find("book").filter(even());
        Match x4 = $(document).find("book").filter(odd());

// This will get all book ID's
        List<String> ids = $(document).find("book").ids();

// This will get all books with ID = 1 or ID = 2
        Match x5 = $(document).find("book").filter(ids("1", "2"));

// Or, use css-selector syntax:
        Match x6 = $(document).find("book#1, book#2");

// This will use XPath to find books with ID = 1 or ID = 2
        Match x7 = $(document).xpath("//book[@id = 1 or @id = 2]");

        // This will add a new book
        $(document).find("books").append("<book id=\"5\"><name>Harry Potter</name></book>");

// But so does this
        $(document).find("book").filter(ids("5")).after("<book id=\"6\"/>");

// This will remove book ID = 1
        $(document).find("book").filter(ids("1")).remove();

// Or this
        $(document).find("book").remove(ids("1"))*/;
    }
}
