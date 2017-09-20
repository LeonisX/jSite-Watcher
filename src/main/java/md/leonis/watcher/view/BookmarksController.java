package md.leonis.watcher.view;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import md.leonis.watcher.config.Config;
import md.leonis.watcher.domain.Bookmark;
import md.leonis.watcher.util.SubPane;
import netscape.javascript.JSException;
import netscape.javascript.JSObject;
import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Jsoup;
import org.w3c.dom.Document;
import sun.misc.IOUtils;

public class BookmarksController extends SubPane {

    @FXML
    public WebView webView;

    @FXML
    private TableView<Bookmark> bookmarksTableView;

    @FXML
    private TableColumn<Bookmark, String> folderColumn;
    @FXML
    private TableColumn<Bookmark, Integer> totalColumn;

    public static final String DEFAULT_JQUERY_MIN_VERSION = "1.7.2";
    public static final String JQUERY_LOCATION = "http://code.jquery.com/jquery-1.7.2.min.js";


    @FXML
    private void initialize() throws Exception {
        bookmarksTableView.setItems(Config.bookmarkObservableList);

        folderColumn.setPrefWidth(120);
        folderColumn.setCellValueFactory(new PropertyValueFactory<>("title"));

        totalColumn.setPrefWidth(40);
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("url"));

        Connection conn = Jsoup.connect("http://tv-games.ru").ignoreContentType(true).method(Method.GET);
        Connection.Response response = conn.execute();

        String content = highlight(response.body(), "а");

        WebEngine webEngine = webView.getEngine();

        webEngine.loadContent(content);
    }

    private String highlight(String body, String text) {
        return body.replace(text, "<b style='color: red; background-color: yellow'>" + text + "</b>");
    }
}
