package md.leonis.watcher.view;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import md.leonis.watcher.config.Config;
import md.leonis.watcher.domain.Bookmark;
import md.leonis.watcher.domain.Category;
import md.leonis.watcher.service.BookmarksService;
import md.leonis.watcher.utils.Comparator;
import md.leonis.watcher.utils.JavaFxUtils;
import md.leonis.watcher.utils.VideoUtils;
import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Jsoup;

import static md.leonis.watcher.utils.JavaFxUtils.registerController;

public class MainStageController {

    @FXML
    private Accordion accordion;

    @FXML
    private Hyperlink settingsHyperlink;

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

    private BookmarksService bookmarksService;

    public void setBookmarksService(BookmarksService bookmarksService) {
        this.bookmarksService = bookmarksService;
        registerController(this);
    }

    @FXML
    private void initialize() {
        accordion.setExpandedPane(accordion.getPanes().get(0));

        folderColumn.setPrefWidth(120);
        folderColumn.setCellValueFactory(
                (TreeTableColumn.CellDataFeatures<Category, String> param) -> new ReadOnlyStringWrapper(
                        param.getValue().getValue().getTitle()));

        totalColumn.setPrefWidth(40);
        totalColumn.setCellValueFactory(
                (TreeTableColumn.CellDataFeatures<Category, Integer> param) -> new ReadOnlyObjectWrapper<>(
                        param.getValue().getChildren().size()));

        root.setExpanded(true);
        categoriesTreeTableView.setRoot(root);
        categoriesTreeTableView.setShowRoot(false);
        //TODO MAP
        Config.categories.forEach((employee) -> root.getChildren().add(new TreeItem<>(employee)));

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

    public void addBookmark() {
        JavaFxUtils.showWindow("frame/AddBookmark.fxml", "Add new bookmark");
        //TODO
    }

    public void checkBookmark() throws IOException {
        for (Bookmark bookmark : bookmarksService.getBookmarks()) {
            System.out.print(bookmark.getTitle() + ": ");
            doCheck(bookmark);
        }
    }

    private void doCheck(Bookmark bookmark) throws IOException {
        if (bookmark.getDate() == null) {
            savePage(String.valueOf(bookmark.getId()));
            //TODO update date, and in db
            bookmark.setDate(new Date());
            System.out.println("unchanged");
        } else {
            System.out.println("changed");
            savePage("tmp");
            String oldPage = new String(Files.readAllBytes(Paths.get("/home/leonidstavila/" + bookmark.getId() + ".html")));
            String newPage = new String(Files.readAllBytes(Paths.get("/home/leonidstavila/tmp.html")));
            if (!Comparator.compare(oldPage, newPage)) {
                //TODO notify
                JavaFxUtils.showAlert("Changed page: " + bookmark.getTitle(), "The page was changed", bookmark.getUrl(), AlertType.INFORMATION);
                //TODO update date, and in db
                Files.move(Paths.get("/home/leonidstavila/tmp.html"), Paths.get("/home/leonidstavila/" + bookmark.getId() + ".html"), StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    private void savePage(String name) throws IOException {
        Connection conn = Jsoup.connect("http://tv-games.ru").ignoreContentType(true).method(Method.GET);
        Connection.Response response = conn.execute();
        byte[] raw = response.bodyAsBytes();
        Path path = Paths.get("/home/leonidstavila/" + name + ".html");
        Files.write(path, raw);
    }
}
