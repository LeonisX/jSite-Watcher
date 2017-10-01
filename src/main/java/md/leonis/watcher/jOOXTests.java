package md.leonis.watcher;

import javafx.application.Application;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.scene.web.WebEngine;
import javafx.stage.Stage;
import md.leonis.wrapper.DocumentWrapper;
import org.joox.Match;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;

import static org.joox.JOOX.*;

public class jOOXTests extends Application {

    public static void main(String[] args) throws IOException, SAXException {
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
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

        //Thread.sleep(5000);

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
