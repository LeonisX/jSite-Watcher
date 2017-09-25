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
import md.leonis.watcher.domain.Bookmark;
import md.leonis.watcher.domain.BookmarkStatus;
import md.leonis.watcher.domain.Category;
import md.leonis.watcher.service.BookmarksService;
import md.leonis.watcher.utils.Comparator;
import md.leonis.watcher.utils.DiffUtils;
import md.leonis.watcher.utils.JavaFxUtils;
import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toMap;
import static md.leonis.watcher.config.Config.HOME;
import static md.leonis.watcher.service.BookmarksService.currentBookmark;
import static md.leonis.watcher.service.BookmarksService.textList;
import static md.leonis.watcher.utils.JavaFxUtils.registerController;
import static org.jsoup.helper.StringUtil.isBlank;

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

        categoriesTreeTableView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    //TODO show bookmarks
                    System.out.println(newValue.getValue().getTitle());
                });


        root.setExpanded(true);
        categoriesTreeTableView.setRoot(root);
        categoriesTreeTableView.setShowRoot(false);

        //TODO MAP
        Config.categories.forEach((employee) -> root.getChildren().add(new TreeItem<>(employee)));
    }

    public void addBookmark() {
        JavaFxUtils.showWindow("frame/AddBookmark.fxml", "Add new bookmark");
        //TODO
    }

    public void checkBookmarks() throws IOException {
        for (Bookmark bookmark : bookmarksService.getBookmarks()) {
            doCheck(bookmark);
            JavaFxUtils.getController(BookmarksController.class).bookmarksTableView.refresh();
        }
        JavaFxUtils.getController(BookmarksController.class).tableView.setUserData(null);
        JavaFxUtils.getController(BookmarksController.class).webView.setUserData(null);
        //TODO refresh current bookmark if not SAME
        if (currentBookmark != null/* && JavaFxUtils.currentBookmark.getStatus() == BookmarkStatus.CHANGED*/) {
            JavaFxUtils.getController(BookmarksController.class).refresh();
        }
    }

    private void doCheck(Bookmark bookmark) throws IOException {
        //TODO preflight
        if (bookmark.getStatus() == BookmarkStatus.NEW) {
            String charset = savePage(String.valueOf(bookmark.getId()), bookmark.getUrl());
            //TODO update date, and charset in db
            bookmark.setDate(new Date());
            bookmark.setCharset(charset);
            bookmark.setStatus(BookmarkStatus.INITIALIZED);
            System.out.println("initialized");
        } else {
            File tempFile = savePageToTmp(bookmark.getUrl());

            Path oldFile = Paths.get(HOME + bookmark.getId() + ".html");

            Document doc = Jsoup.parse(new File(HOME + bookmark.getId() + ".html"), bookmark.getCharset(), bookmark.getUrl());
            List<String> leftList = walkTree(doc.body(), new ArrayList<>());

            doc = Jsoup.parse(tempFile, null, bookmark.getUrl());
            List<String> rightList = walkTree(doc.body(), new ArrayList<>());

            Map<Integer, String> leftMap = toIndexedMap(leftList);
            Map<Integer, String> rightMap = toIndexedMap(rightList);

            System.out.println(leftMap);

            textList = DiffUtils.diff(leftMap, rightMap);

            BasicFileAttributes attr = Files.readAttributes(oldFile, BasicFileAttributes.class);

            String creationDate = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
                    .withZone(ZoneId.systemDefault())
                    .format(attr.creationTime().toInstant());

            JavaFxUtils.getController(BookmarksController.class).leftCol.setText("Old page (" + creationDate + ")");

            //TODO
            if (!Comparator.compare(textList)) {
                System.out.println("changed");
                //TODO notify
                //JavaFxUtils.showAlert("Changed page: " + bookmark.getTitle(), "The page was changed", bookmark.getUrl(), AlertType.INFORMATION);
                Files.move(Paths.get(HOME + bookmark.getId() + ".html"), Paths.get(HOME + bookmark.getId() + "o.html"), StandardCopyOption.REPLACE_EXISTING);
                Files.move(tempFile.toPath(), Paths.get(HOME + bookmark.getId() + ".html"), StandardCopyOption.REPLACE_EXISTING);
                //TODO update date, and status in db
                bookmark.setDate(new Date());
                bookmark.setStatus(BookmarkStatus.CHANGED);
            } else {
                if (bookmark.getStatus() == BookmarkStatus.INITIALIZED) {
                    Files.copy(Paths.get(HOME + bookmark.getId() + ".html"), Paths.get(HOME + bookmark.getId() + "o.html"), StandardCopyOption.REPLACE_EXISTING);
                }
                if (bookmark.getStatus() != BookmarkStatus.UNCHANGED) {
                    bookmark.setStatus(BookmarkStatus.UNCHANGED);
                    //TODO update date, and status in db
                }
                System.out.println("unchanged");
            }
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

    private String savePage(String name, String url) throws IOException {
        Connection conn = Jsoup.connect(url).ignoreContentType(true).method(Method.GET);
        Connection.Response response = conn.execute();
        byte[] raw = response.bodyAsBytes();
        Path path = Paths.get(HOME + name + ".html");
        Files.write(path, raw);
        return response.charset();
    }

    private File savePageToTmp(String url) throws IOException {
        Connection conn = Jsoup.connect(url).ignoreContentType(true).method(Method.GET);
        Connection.Response response = conn.execute();
        byte[] raw = response.bodyAsBytes();
        File tempFile = File.createTempFile("jsw-", "", null);
        FileOutputStream fos = new FileOutputStream(tempFile);
        fos.write(raw);
        return tempFile;
    }
}
