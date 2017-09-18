package md.leonis.watcher;

import md.leonis.watcher.domain.TagTreeItem;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.IOException;

public class MainTreeView extends Application {

    private static final ClassLoader classLoader = MainTreeView.class.getClassLoader();

    private final Node rootIcon = new ImageView(
            new Image(classLoader.getResourceAsStream("folder_red_open.png"))
    );

    private Label label = new Label();

    public static void main(String[] args) throws IOException {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        primaryStage.setTitle("Tree View Sample");

        Document doc = Jsoup.parse(new File(classLoader.getResource("page.html").getFile()), null);

        TagTreeItem<Element> rootItem = new TagTreeItem<>(doc.body(), rootIcon);
        rootItem.setExpanded(true);

        rootItem.addChildren();

        TreeView<String> tree = new TreeView<>(rootItem);

        EventHandler<MouseEvent> mouseEventHandle = this::handleMouseMoved;
        tree.addEventHandler(MouseEvent.MOUSE_MOVED, mouseEventHandle);

        BorderPane root = new BorderPane();
        root.setCenter(tree);
        root.setBottom(label);
        primaryStage.setScene(new Scene(root, 600, 650));
        primaryStage.show();
    }

    private void handleMouseMoved(MouseEvent event) {
        Node node = event.getPickResult().getIntersectedNode();
        if (node instanceof TreeCell)  {
            TreeCell treeCell = (TreeCell) node;
            System.out.println(((TagTreeItem) treeCell.getTreeItem()).getElement().ownText());
            label.setText(((TagTreeItem) treeCell.getTreeItem()).getParents());
        }
    }
}
