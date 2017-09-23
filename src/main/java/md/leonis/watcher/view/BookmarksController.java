package md.leonis.watcher.view;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Control;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Callback;
import md.leonis.watcher.MainTableView;
import md.leonis.watcher.domain.Bookmark;
import md.leonis.watcher.domain.DiffStatus;
import md.leonis.watcher.domain.TextDiff;
import md.leonis.watcher.utils.DiffUtils;
import md.leonis.watcher.utils.SubPane;
import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.util.*;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toMap;
import static md.leonis.watcher.utils.JavaFxUtils.bookmarksService;
import static md.leonis.watcher.utils.JavaFxUtils.registerController;
import static org.jsoup.helper.StringUtil.isBlank;

public class BookmarksController extends SubPane {


    //TODO in config
    private static final ClassLoader classLoader = MainTableView.class.getClassLoader();

    @FXML
    TableView<TextDiff> tableView;

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

        Connection conn = Jsoup.connect("http://yandex.ru").ignoreContentType(true).method(Method.GET);
        Connection.Response response = conn.execute();

        String content = highlight(response.body(), "а");

        WebEngine webEngine = webView.getEngine();

        webEngine.loadContent(content);
        registerController(this);




        // TABLE

        Document doc = Jsoup.parse(new File(classLoader.getResource("page.html").getFile()), null);

        //tableView.setSelectionModel(null);

        // Create column UserName (Data type of String).
        TableColumn<TextDiff, String> leftCol = new TableColumn<>("Old page (date...)");
        leftCol.prefWidthProperty().bind(tableView.widthProperty().divide(2));
        leftCol.setId("leftCol");
        leftCol.setCellFactory(cellFactory());

        //TODO maximal settings in fxml
        //TODO show diff in text (in future)
        // Create column Email (Data type of String).
        TableColumn<TextDiff, String> rightCol = new TableColumn<>("New page (date...)");
        rightCol.prefWidthProperty().bind(tableView.widthProperty().divide(2));
        rightCol.setId("rightCol");
        rightCol.setCellFactory(cellFactory());

        leftCol.setCellValueFactory(new PropertyValueFactory<>("leftText"));
        rightCol.setCellValueFactory(new PropertyValueFactory<>("rightText"));

        Collection<String> leftList = walkTree(doc.body(), new ArrayList<>());

        //List<PageText> textList = mergeLists(leftList, leftList);

        List<String> left = Arrays.asList("aaa", "bbb", "ccc", "diff2", "When adding text, you can also set some of its properties.", "sea", "cae", "eee", "changed");
        List<String> right = Arrays.asList("000", "111", "aaa", "ccc", "diff", "When adding text, you can also set some of its", "ddd", "added1", "added2", "eee", "change");

        Map<Integer, String> leftMap = toIndexedMap(left);
        Map<Integer, String> rightMap = toIndexedMap(right);

        List<TextDiff> textList = DiffUtils.diff(leftMap, rightMap);

        // Display row data
        ObservableList<TextDiff> list = FXCollections.observableList(textList);

        tableView.setItems(list);

        tableView.getColumns().add(leftCol);
        tableView.getColumns().add(rightCol);
    }

    private String highlight(String body, String text) {
        return body.replace(text, "<b style='color: red; background-color: yellow'>" + text + "</b>");
    }


    private static Callback<TableColumn<TextDiff, String>, TableCell<TextDiff, String>> cellFactory() {
        return new Callback<TableColumn<TextDiff, String>, TableCell<TextDiff, String>>() {
            @Override
            public TableCell<TextDiff, String> call(TableColumn<TextDiff, String> param) {
                return new TableCell<TextDiff, String>() {

                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        Text text = new Text();
                        text.wrappingWidthProperty().bind(getTableColumn().widthProperty());
                        text.textProperty().bind(itemProperty());

                        setPrefHeight(Control.USE_COMPUTED_SIZE);
                        setGraphic(text);

                        if (getTableRow().getIndex() >= 0 && getTableRow().getIndex() < getTableView().getItems().size()) {
                            if (getTableView().getItems().get(getTableRow().getIndex()).getStatus() == DiffStatus.ADDED) {
                                //this.setStyle("-fx-background-color: rgb(200,255,200);");
                                //this.setStyle("-fx-text-fill: red;");
                                text.setFill(Color.GREEN);
                                this.setStyle("-fx-font-weight: bold; -fx-background-color: #F0FFF0;");
                            }
                            if (getTableView().getItems().get(getTableRow().getIndex()).getStatus() == DiffStatus.CHANGED) {
                                text.setFill(Color.BLUE);
                                this.setStyle("-fx-font-weight: bold; -fx-background-color: #F0F0FF;");
                            }
                            if (getTableView().getItems().get(getTableRow().getIndex()).getStatus() == DiffStatus.DELETED) {
                                text.setFill(Color.RED);
                                text.setStrikethrough(true);
                                this.setStyle("-fx-font-weight: bold; -fx-background-color: #FFF0F0;");
                            }
                            //this.setBackground();
                            //this.setStyle("-fx-background-color: #00FFFF;");
                            this.setText(empty ? "" : getItem());
                        }
                    }
                };
            }
        };
    }


    private static Map<Integer, String> toIndexedMap(List<String> stringList) {
        return IntStream.range(0, stringList.size())
                .boxed()
                .collect(toMap(idx -> idx, stringList::get));
    }

/*    private List<PageText> mergeLists(Collection<String> leftList, Collection<String> rightList) {
        return leftList.stream().map(s -> new PageText(s, s)).collect(Collectors.toList());
    }*/


    private static Collection<String> walkTree(Element element, Collection<String> collection) {
        if (!isBlank(element.ownText())) {
            collection.add(element.ownText());
        }
        element.children().forEach(e -> walkTree(e, collection));
        return collection;
    }
}
