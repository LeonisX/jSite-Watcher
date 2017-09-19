package md.leonis.watcher.view;

import java.util.Arrays;
import java.util.List;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.Accordion;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import md.leonis.watcher.util.VideoUtils;

public class MainStageController {

    @FXML
    private Accordion accordion;
    @FXML
    private Hyperlink settingsHyperlink;

    @FXML
    private TreeTableView<Tree> categoriesTreeTableView;
    @FXML
    private TreeTableColumn<Tree, String> folderColumn;
    @FXML
    private TreeTableColumn<Tree, Integer> totalColumn;

    private List<Tree> employees = Arrays.<Tree>asList(
            new Tree("Ethan Williams", 1),
            new Tree("Emma Jones", 2),
            new Tree("Michael Brown", 3),
            new Tree("Anna Black", 4),
            new Tree("Rodger York", 5),
            new Tree("Susan Collins", 6));

    private final ImageView depIcon = new ImageView (
            new Image(MainStageController.class.getClassLoader().getResourceAsStream("folder_red_open.png"))
    );

    private final TreeItem<Tree> root =
            new TreeItem<>(new Tree("Sales Department", 0), depIcon);


    @FXML
    private void initialize() {
        accordion.setExpandedPane(accordion.getPanes().get(0));

        folderColumn.setPrefWidth(120);
        folderColumn.setCellValueFactory(
                (TreeTableColumn.CellDataFeatures<Tree, String> param) -> new ReadOnlyStringWrapper(
                        param.getValue().getValue().getName()));

        totalColumn.setPrefWidth(40);
        totalColumn.setCellValueFactory(
                (TreeTableColumn.CellDataFeatures<Tree, Integer> param) -> new ReadOnlyObjectWrapper<>(
                        param.getValue().getValue().getTotal()));

        root.setExpanded(true);
        categoriesTreeTableView.setRoot(root);
        categoriesTreeTableView.setShowRoot(false);
        employees.forEach((employee) -> root.getChildren().add(new TreeItem<>(employee)));
    }

    @FXML
    private void addVideo() {
        VideoUtils.action = VideoUtils.Actions.ADD;
        VideoUtils.showAddVideo();
    }

    @FXML
    private void listVideos() {
        VideoUtils.showListVideous();
    }

    public class Tree {

        private String title;
        private int total;

        private Tree(String title, int total) {
            this.title = title;
            this.total = total;
        }

        public String getName() {
            return title;
        }
        public void setName(String fTitle) {
            title = fTitle;
        }
        public int getTotal() {
            return total;
        }
        public void setTotal(int fTotal) {
            total = fTotal;
        }
    }
}