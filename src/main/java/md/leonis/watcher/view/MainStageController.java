package md.leonis.watcher.view;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.Accordion;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import md.leonis.watcher.config.Config;
import md.leonis.watcher.domain.Category;
import md.leonis.watcher.utils.JavaFxUtils;

import java.io.IOException;

import static md.leonis.watcher.utils.JavaFxUtils.bookmarksService;
import static md.leonis.watcher.utils.JavaFxUtils.registerController;

public class MainStageController {

    @FXML
    private Accordion accordion;

    @FXML
    private TreeTableView<Category> categoriesTreeTableView;

    @FXML
    private TreeTableColumn<Category, String> folderColumn;

    @FXML
    private TreeTableColumn<Category, Integer> totalColumn;

    private final ImageView depIcon = new ImageView (
            new Image(MainStageController.class.getClassLoader().getResourceAsStream("folder_red_open.png"))
    );

    private final TreeItem<Category> root =
            new TreeItem<>(new Category(0, 0, "r00t", ""), depIcon);

    @FXML
    private void initialize() throws IOException {
        registerController(this);
        accordion.setExpandedPane(accordion.getPanes().get(0));

        folderColumn.setPrefWidth(120);
        folderColumn.setCellValueFactory(
                (TreeTableColumn.CellDataFeatures<Category, String> param) -> new ReadOnlyStringWrapper(
                        param.getValue().getValue().getTitle()));

        totalColumn.setPrefWidth(40);
        totalColumn.setCellValueFactory(
                (TreeTableColumn.CellDataFeatures<Category, Integer> param) -> new ReadOnlyObjectWrapper<>(
                        param.getValue().getChildren().size()));

        categoriesTreeTableView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    //TODO show bookmarks
                    System.out.println(newValue.getValue().getTitle());
                });


        root.setExpanded(true);
        categoriesTreeTableView.setRoot(root);
        categoriesTreeTableView.setShowRoot(false);

        //TODO MAP
        Config.categories.forEach((category) -> root.getChildren().add(new TreeItem<>(category)));
    }

    public void addBookmark() {
        JavaFxUtils.showWindow("frame/AddBookmark.fxml", "Add new bookmark");
        //TODO refresh?
    }

    public void checkBookmarks() throws IOException {
        bookmarksService.checkBookmarks();
    }

}
