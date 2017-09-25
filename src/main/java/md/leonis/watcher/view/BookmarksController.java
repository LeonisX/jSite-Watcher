package md.leonis.watcher.view;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;
import javafx.util.Callback;
import md.leonis.watcher.domain.Bookmark;
import md.leonis.watcher.domain.DiffStatus;
import md.leonis.watcher.domain.TextDiff;
import md.leonis.watcher.utils.SubPane;

import static md.leonis.watcher.utils.JavaFxUtils.bookmarksService;
import static md.leonis.watcher.utils.JavaFxUtils.registerController;

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
    private TableView<Bookmark> bookmarksTableView;

    @FXML
    private TableColumn<Bookmark, String> titleColumn;

    @FXML
    private TableColumn<Bookmark, Integer> urlColumn;

    @FXML
    private void initialize() throws Exception {
        bookmarksTableView.setItems(bookmarksService.getBookmarkObservableList());

        bookmarksTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            //TODO change current bookmark related data
            //TODO show in current pane control
            switch (tabPane.getSelectionModel().getSelectedIndex()) {
                case 0:
                    //TODO webview
                    break;
                case 1:
                    //TODO tableview
                    break;
                case 2:
                    break;
            }

        });

        registerController(this);

        leftCol.prefWidthProperty().bind(tableView.widthProperty().divide(2));
        leftCol.setCellFactory(cellFactory());

        rightCol.prefWidthProperty().bind(tableView.widthProperty().divide(2));
        rightCol.setCellFactory(cellFactory());
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
}
