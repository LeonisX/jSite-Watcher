package md.leonis.watcher.view;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import md.leonis.watcher.config.Config;
import md.leonis.watcher.domain.Bookmark;
import md.leonis.watcher.util.SubPane;

public class BookmarksController extends SubPane {

    @FXML
    private TableView<Bookmark> bookmarksTableView;

    @FXML
    private TableColumn<Bookmark, String> folderColumn;
    @FXML
    private TableColumn<Bookmark, Integer> totalColumn;

    @FXML
    private void initialize() {
        bookmarksTableView.setItems(Config.bookmarkObservableList);

        folderColumn.setPrefWidth(120);
        folderColumn.setCellValueFactory(new PropertyValueFactory<>("title"));

        totalColumn.setPrefWidth(40);
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("url"));
    }

}
