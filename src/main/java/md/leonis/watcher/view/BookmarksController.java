package md.leonis.watcher.view;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Callback;
import md.leonis.watcher.domain.Bookmark;
import md.leonis.watcher.domain.BookmarkStatus;
import md.leonis.watcher.domain.DiffStatus;
import md.leonis.watcher.domain.TextDiff;
import md.leonis.watcher.utils.DiffUtils;
import md.leonis.watcher.utils.SubPane;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toMap;
import static md.leonis.watcher.config.Config.HOME;
import static md.leonis.watcher.service.BookmarksService.*;
import static md.leonis.watcher.utils.JavaFxUtils.bookmarksService;
import static md.leonis.watcher.utils.JavaFxUtils.registerController;
import static org.jsoup.helper.StringUtil.isBlank;

public class BookmarksController extends SubPane {

    @FXML
    public TableColumn<TextDiff, String> leftCol;

    @FXML
    public TableColumn<TextDiff, String> rightCol;

    @FXML
    private TabPane tabPane;

    @FXML
    TableView<TextDiff> tableView;

    @FXML
    public WebView webView;

    @FXML
    public TableView<Bookmark> bookmarksTableView;

    @FXML
    private TableColumn<Bookmark, String> titleColumn;

    @FXML
    private TableColumn<Bookmark, Integer> urlColumn;

    @FXML
    private TableColumn<Bookmark, String> statusColumn;

    @FXML
    private void initialize() throws Exception {
        //webView.getEngine().setJavaScriptEnabled(true);
        bookmarksTableView.setItems(bookmarksService.getBookmarkObservableList());

        bookmarksTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            //TODO show in current pane control
            currentBookmark = newSelection;
            try {
                refresh();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        titleColumn.setCellFactory(cellFactory2());

        tabPane.getSelectionModel().selectedItemProperty().addListener((ov, oldTab, newTab) -> {
            try {
                refresh();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        registerController(this);

        leftCol.prefWidthProperty().bind(tableView.widthProperty().divide(2));
        leftCol.setCellFactory(cellFactory());

        rightCol.prefWidthProperty().bind(tableView.widthProperty().divide(2));
        rightCol.setCellFactory(cellFactory());

        tableView.setItems(list);
    }

    public void refresh() throws IOException {
        //System.out.println(webView.getUserData());
        if (currentBookmark == null || currentBookmark.getStatus() == BookmarkStatus.NEW) {
            return;
        }
        switch (tabPane.getSelectionModel().getSelectedIndex()) {
            case 0:
                if (webView.getUserData() == null || !webView.getUserData().equals(currentBookmark.getId())) {
                    webView.setUserData(currentBookmark.getId());
                    //TODO refresh
                    String[] content = new String[]{String.join("\n",
                            Files.readAllLines(Paths.get(HOME + currentBookmark.getId() + ".html"), Charset.forName(currentBookmark.getCharset())))};

                    textList.stream()
                            .filter(t -> t.getStatus().equals(DiffStatus.CHANGED) || t.getStatus().equals(DiffStatus.ADDED))
                            .forEach(t -> highlight(content, t.getRightText()));

                    WebEngine webEngine = webView.getEngine();
                    webEngine.loadContent(content[0]);
                }
                break;
            case 1:
                if (tableView.getUserData() == null || !tableView.getUserData().equals(currentBookmark.getId())) {
                    tableView.setUserData(currentBookmark.getId());
                    //TODO refresh
                    List<String> leftList;
                    //TODO optimize
                    if (currentBookmark.getStatus() == BookmarkStatus.CHANGED || currentBookmark.getStatus() == BookmarkStatus.UNCHANGED
                            || currentBookmark.getStatus() == BookmarkStatus.ERROR) {
                        Document doc = Jsoup.parse(new File(HOME + currentBookmark.getId() + "o.html"), currentBookmark.getCharset(), currentBookmark.getUrl());
                        leftList = walkTree(doc.body(), new ArrayList<>());
                    } else {
                        leftList = new ArrayList<>();
                    }

                    List<String> rightList;
                    if (currentBookmark.getStatus() != BookmarkStatus.NEW) {
                        Document doc = Jsoup.parse(new File(HOME + currentBookmark.getId() + ".html"), currentBookmark.getCharset(), currentBookmark.getUrl());
                        rightList = walkTree(doc.body(), new ArrayList<>());
                    } else {
                        rightList = new ArrayList<>();
                    }

                    Map<Integer, String> leftMap = toIndexedMap(leftList);
                    Map<Integer, String> rightMap = toIndexedMap(rightList);

                    textList = DiffUtils.diff(leftMap, rightMap);

                    list = FXCollections.observableList(textList);

                    tableView.setItems(list);
                }
                break;
            case 2:
                break;
        }
    }


    private static Map<Integer, String> toIndexedMap(List<String> stringList) {
        return IntStream.range(0, stringList.size())
                .boxed()
                .collect(toMap(idx -> idx, stringList::get));
    }

    private static List<String> walkTree(Element element, List<String> collection) {
        if (!isBlank(element.ownText())) {
            collection.add(element.ownText());
        }
        element.children().forEach(e -> walkTree(e, collection));
        return collection;
    }

    private void highlight(String[] body, String text) {
        body[0] = body[0].replace(text, "<b style='color: red; background-color: yellow'>" + text + "</b>");
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
                            if (getTableView().getItems().get(getTableRow().getIndex()).getStatus() == DiffStatus.SAME) {
                                text.setFill(Color.BLACK);
                                this.setStyle("-fx-font-weight: normal; -fx-background-color: #FFFFFF;");
                            }
                            this.setText(empty ? "" : getItem());
                        }
                    }
                };
            }
        };
    }

    private static Callback<TableColumn<Bookmark, String>, TableCell<Bookmark, String>> cellFactory2() {
        return new Callback<TableColumn<Bookmark, String>, TableCell<Bookmark, String>>() {
            @Override
            public TableCell<Bookmark, String> call(TableColumn<Bookmark, String> param) {
                return new TableCell<Bookmark, String>() {

                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        Text text = new Text();
                        text.wrappingWidthProperty().bind(getTableColumn().widthProperty());
                        text.textProperty().bind(itemProperty());

                        setPrefHeight(Control.USE_COMPUTED_SIZE);
                        setGraphic(text);

                        if (getTableRow().getIndex() >= 0 && getTableRow().getIndex() < getTableView().getItems().size()) {
                            if (getTableView().getItems().get(getTableRow().getIndex()).getStatus() == BookmarkStatus.CHANGED) {
                                text.setFill(Color.GREEN);
                                this.setStyle("-fx-font-weight: bold;");
                            } else if (getTableView().getItems().get(getTableRow().getIndex()).getStatus() == BookmarkStatus.ERROR) {
                                text.setFill(Color.RED);
                                this.setStyle("-fx-font-weight: normal;");
                            } else if (getTableView().getItems().get(getTableRow().getIndex()).getStatus() == BookmarkStatus.NEW) {
                                text.setFill(Color.BLUE);
                                this.setStyle("-fx-font-weight: normal;");
                            } else {
                                text.setFill(Color.BLACK);
                                this.setStyle("-fx-font-weight: normal;");
                            }
                            this.setText(empty ? "" : getItem());
                        }
                    }
                };
            }
        };
    }
}
