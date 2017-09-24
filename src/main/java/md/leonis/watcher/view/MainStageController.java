package md.leonis.watcher.view;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import md.leonis.watcher.MainTableView;
import md.leonis.watcher.config.Config;
import md.leonis.watcher.domain.Bookmark;
import md.leonis.watcher.domain.Category;
import md.leonis.watcher.domain.DiffStatus;
import md.leonis.watcher.domain.TextDiff;
import md.leonis.watcher.service.BookmarksService;
import md.leonis.watcher.utils.Comparator;
import md.leonis.watcher.utils.DiffUtils;
import md.leonis.watcher.utils.JavaFxUtils;
import md.leonis.watcher.utils.VideoUtils;
import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static md.leonis.watcher.config.Config.HOME;
import static md.leonis.watcher.utils.JavaFxUtils.registerController;
import static org.jsoup.helper.StringUtil.isBlank;

public class MainStageController {


    //TODO in config
    private static final ClassLoader classLoader = MainTableView.class.getClassLoader();

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

        //TODO preflight
        //TODO jsoup diff, webView, treeView
        if (bookmark.getDate() == null) {
            savePage(String.valueOf(bookmark.getId()), bookmark.getUrl());
            //TODO update date, and in db
            bookmark.setDate(new Date());
            System.out.println("initialized");
        } else {
            Connection conn = Jsoup.connect(bookmark.getUrl()).ignoreContentType(true).method(Method.GET);
            Connection.Response response = conn.execute();
            byte[] raw = response.bodyAsBytes();
            File tempFile = File.createTempFile("jsw-", "", null);
            FileOutputStream fos = new FileOutputStream(tempFile);
            fos.write(raw);
            //return tempFile.toPath();

            Path tmpFile = tempFile.toPath();
            String oldPage = new String(Files.readAllBytes(Paths.get(HOME + bookmark.getId() + ".html")));
            String newPage = new String(Files.readAllBytes(tmpFile));


            Document doc = Jsoup.parse(new File(HOME + bookmark.getId() + ".html"), null, HOME + bookmark.getId() + ".html");

            List<String> leftList = walkTree(doc.body(), new ArrayList<>());

            //doc = Jsoup.parse(tmpFile.toFile(), null);

            doc = response.parse();

            List<String> rightList = walkTree(doc.body(), new ArrayList<>());

            //List<PageText> textList = mergeLists(leftList, leftList);

            //List<String> left = Arrays.asList("aaa", "bbb", "ccc", "diff2", "When adding text, you can also set some of its properties.", "sea", "cae", "eee", "changed");
            //List<String> right = Arrays.asList("000", "111", "aaa", "ccc", "diff", "When adding text, you can also set some of its", "ddd", "added1", "added2", "eee", "change");

            Map<Integer, String> leftMap = toIndexedMap(leftList);
            Map<Integer, String> rightMap = toIndexedMap(rightList);

            List<TextDiff> textList = DiffUtils.diff(leftMap, rightMap);

            // Display row data
            ObservableList<TextDiff> list = FXCollections.observableList(textList);

            TableView<TextDiff> tableView = JavaFxUtils.getController(BookmarksController.class).tableView;
            tableView.setItems(list);



            // WEB VIEW
            String[] content = new String[]{response.body()};
/*

            textList = textList.stream()
                    .filter(t -> t.getStatus().equals(DiffStatus.CHANGED) || t.getStatus().equals(DiffStatus.ADDED)).collect(toList());

*/


            textList.stream()
                    .filter(t -> t.getStatus().equals(DiffStatus.CHANGED) || t.getStatus().equals(DiffStatus.ADDED))
                    .forEach(t -> highlight(content, t.getRightText()));


            //String content = highlight(response.body(), "а");

            WebView webView = JavaFxUtils.getController(BookmarksController.class).webView;

            WebEngine webEngine = webView.getEngine();

            webEngine.loadContent(content[0]);







            if (!Comparator.compare(oldPage, newPage)) {
                System.out.println("changed");
                //TODO notify
                //JavaFxUtils.showAlert("Changed page: " + bookmark.getTitle(), "The page was changed", bookmark.getUrl(), AlertType.INFORMATION);
                Files.move(Paths.get(HOME + bookmark.getId() + ".html"), Paths.get(HOME + bookmark.getId() + "o.html"), StandardCopyOption.REPLACE_EXISTING);
                Files.move(tmpFile, Paths.get(HOME + bookmark.getId() + ".html"), StandardCopyOption.REPLACE_EXISTING);
                //TODO update date, and in db
                bookmark.setDate(new Date());
            } else {
                System.out.println("unchanged");
            }
        }
    }


    private void highlight(String[] body, String text) {
        body[0] = body[0].replace(text, "<b style='color: red; background-color: yellow'>" + text + "</b>");
    }


    private static Map<Integer, String> toIndexedMap(List<String> stringList) {
        return IntStream.range(0, stringList.size())
                .boxed()
                .collect(toMap(idx -> idx, stringList::get));
    }

/*    private List<PageText> mergeLists(Collection<String> leftList, Collection<String> rightList) {
        return leftList.stream().map(s -> new PageText(s, s)).collect(Collectors.toList());
    }*/


    private static List<String> walkTree(Element element, List<String> collection) {
        if (!isBlank(element.ownText())) {
            collection.add(element.ownText());
        }
        element.children().forEach(e -> walkTree(e, collection));
        return collection;
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
