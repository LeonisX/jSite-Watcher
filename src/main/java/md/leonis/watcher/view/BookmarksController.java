package md.leonis.watcher.view;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import md.leonis.watcher.domain.Bookmark;
import md.leonis.watcher.utils.SubPane;
import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Jsoup;

import static md.leonis.watcher.utils.JavaFxUtils.bookmarksService;
import static md.leonis.watcher.utils.JavaFxUtils.registerController;

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
        bookmarksTableView.setItems(bookmarksService.getBookmarkObservableList());

        folderColumn.setPrefWidth(120);
        folderColumn.setCellValueFactory(new PropertyValueFactory<>("title"));

        totalColumn.setPrefWidth(40);
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("url"));

        Connection conn = Jsoup.connect("http://tv-games.ru").ignoreContentType(true).method(Method.GET);
        Connection.Response response = conn.execute();

        String content = highlight(response.body(), "а");

        WebEngine webEngine = webView.getEngine();

        webEngine.loadContent(content);
        registerController(this);
    }

    private String highlight(String body, String text) {
        return body.replace(text, "<b style='color: red; background-color: yellow'>" + text + "</b>");
    }
}
