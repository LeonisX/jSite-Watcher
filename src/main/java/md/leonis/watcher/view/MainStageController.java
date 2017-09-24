package md.leonis.watcher.view;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.util.Date;

import static md.leonis.watcher.config.Config.HOME;
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
    private void initialize() throws IOException {
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
            savePage(String.valueOf(bookmark.getId()), bookmark.getUrl());
            //TODO update date, and in db
            bookmark.setDate(new Date());
            System.out.println("initialized");
        } else {
            Path tmpFile = savePageToTmp(bookmark.getUrl());
            String oldPage = new String(Files.readAllBytes(Paths.get(HOME + bookmark.getId() + ".html")));
            String newPage = new String(Files.readAllBytes(tmpFile));
            if (!Comparator.compare(oldPage, newPage)) {
                System.out.println("changed");
                //TODO notify
                JavaFxUtils.showAlert("Changed page: " + bookmark.getTitle(), "The page was changed", bookmark.getUrl(), AlertType.INFORMATION);
                Files.move(Paths.get(HOME + bookmark.getId() + ".html"), Paths.get(HOME + bookmark.getId() + "o.html"), StandardCopyOption.REPLACE_EXISTING);
                Files.move(tmpFile, Paths.get(HOME + bookmark.getId() + ".html"), StandardCopyOption.REPLACE_EXISTING);
                //TODO update date, and in db
                bookmark.setDate(new Date());
            } else {
                System.out.println("unchanged");
            }
        }
    }

    //TODO
    private void savePage(String name, String url) throws IOException {
        Connection conn = Jsoup.connect(url).ignoreContentType(true).method(Method.GET);
        Connection.Response response = conn.execute();
        byte[] raw = response.bodyAsBytes();
        Path path = Paths.get(HOME + name + ".html");
        Files.write(path, raw);
    }

    private Path savePageToTmp(String url) throws IOException {
        Connection conn = Jsoup.connect(url).ignoreContentType(true).method(Method.GET);
        Connection.Response response = conn.execute();
        byte[] raw = response.bodyAsBytes();
        File tempFile = File.createTempFile("jsw-", "", null);
        FileOutputStream fos = new FileOutputStream(tempFile);
        fos.write(raw);
        return tempFile.toPath();
    }



}
