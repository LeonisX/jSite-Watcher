package md.leonis.watcher.service;

import com.iciql.Db;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker.State;
import javafx.scene.control.TableView;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import lombok.Getter;
import md.leonis.watcher.domain.*;
import md.leonis.watcher.utils.Comparator;
import md.leonis.watcher.utils.DiffUtils;
import md.leonis.watcher.utils.JavaFxUtils;
import md.leonis.watcher.view.BookmarksController;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toMap;
import static md.leonis.watcher.config.Config.HOME;
import static md.leonis.watcher.utils.JavaFxUtils.bookmarksService;
import static org.jsoup.helper.StringUtil.isBlank;

@Getter
public class BookmarksService {

    private Db db;

    private Rule rule = new Rule(1, 1, RuleType.EXCLUDE_BY_CLASS, "hero");

    public static Bookmark currentBookmark;

    public static List<TextDiff> textList = new ArrayList<>();

    public static ObservableList<TextDiff> list = FXCollections.observableList(textList);

    private List<Rule> rules = Arrays.asList(rule);

    private List<Bookmark> bookmarks = new ArrayList<>(
            Arrays.asList(new Bookmark(1, 1, "http://tv-games.ru", "TiVi", null, BookmarkStatus.NEW, "", "", rules),
                    new Bookmark(4, 1, "http://www.emu-land.net", "mumuland", null, BookmarkStatus.NEW, "", "",
                            new ArrayList<>())/*,
            new Bookmark(2, 1, "http://yandex.ru", "Yasha", null, BookmarkStatus.NEW, "", "", new ArrayList<>()),
            new Bookmark(3, 1, "http://google.com", "Grisha", null, BookmarkStatus.NEW, "", "", new ArrayList<>()),
            new Bookmark(4, 1, "http://www.emu-land.net", "mumuland", null, BookmarkStatus.NEW, "", "", new ArrayList<>()),
            new Bookmark(5, 1, "http://www.emu-russia.net", "emurasha", null, BookmarkStatus.NEW, "", "", new ArrayList<>())*/));

    private ObservableList<Bookmark> bookmarkObservableList = FXCollections.observableArrayList(bookmarks);

    public BookmarksService(DbService dbService) {
        this.db = dbService.getDb();
    }

    public void addBookmark(String title, String url) throws IOException {
        Bookmark bookmark = new Bookmark(0, 0, url, title, null, BookmarkStatus.NEW, "", "", new ArrayList<>());
        long id = db.insertAndGetKey(bookmark);
        bookmark.setId((int) id);
        bookmarkObservableList.add(bookmark);
    }

    public void refreshWebView(WebView webView) {
        try {
            if (webView.getUserData() == null || !webView.getUserData().equals(currentBookmark.getId())) {
                webView.setUserData(currentBookmark.getId());
                String[] content = new String[]{String.join("\n",
                        Files.readAllLines(Paths.get(HOME + currentBookmark.getId() + ".html"),
                                Charset.forName(currentBookmark.getCharset())))};

                if (currentBookmark.getStatus() == BookmarkStatus.CHANGED) {
                    textList.stream()
                            .filter(t -> t.getStatus().equals(DiffStatus.CHANGED) || t.getStatus().equals(DiffStatus.ADDED))
                            .forEach(t -> highlight(content, t.getRightText()));
                }

                //TODO option - load from origin direct

                //TODO rewrite, app preflight do before browser on content[0].
                WebEngine webEngine = webView.getEngine();
                webEngine.loadContent(content[0]);
                webEngine.getLoadWorker().stateProperty().addListener((ov, oldState, newState) -> {
                    if (newState.equals(State.SUCCEEDED)) {
                        correctLinks(webEngine, "link", "href");
                        correctLinks(webEngine, "a", "href");
                        correctLinks(webEngine, "img", "src");
                        correctLinks(webEngine, "script", "src");
                        /*try {
                            TransformerFactory tf = TransformerFactory.newInstance();
                            Transformer transformer = tf.newTransformer();
                            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                            StringWriter writer = new StringWriter();
                            transformer.transform(new DOMSource(webEngine.getDocument()), new StreamResult(writer));
                            String output = writer.getBuffer().toString();
                            System.out.println(output);
                        } catch (TransformerConfigurationException e) {
                            e.printStackTrace();
                        } catch (TransformerException e) {
                            e.printStackTrace();
                        }*/
                    } else {
                        System.out.println(newState);
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void correctLinks(WebEngine webEngine, String tag, String attribute) {
        NodeList nodeList = webEngine.getDocument().getElementsByTagName(tag);
        //TODO process tree, find href, src
        for (int i = 0; i < nodeList.getLength(); i++) {
            if (((org.w3c.dom.Element) nodeList.item(i)).hasAttribute(attribute)) {
                String attr = ((org.w3c.dom.Element) nodeList.item(i)).getAttribute(attribute);
                if (attr.startsWith("//")) {
                    attr = "http:" + attr;
                    ((org.w3c.dom.Element) nodeList.item(i)).setAttribute(attribute, attr);
                }
                if (attr.startsWith("/")) {
                    //TODO correct path (i. .e, remove page name)
                    attr = currentBookmark.getUrl() + attr;
                    ((org.w3c.dom.Element) nodeList.item(i)).setAttribute(attribute, attr);
                }
            }
        }
    }

    private void highlight(String[] body, String text) {
        body[0] = body[0].replace(text, "<b style='color: red; background-color: yellow'>" + text + "</b>");
    }

    public void refreshTableView(TableView<TextDiff> tableView) {
        try {
            if (tableView.getUserData() == null || !tableView.getUserData().equals(currentBookmark.getId())) {
                tableView.setUserData(currentBookmark.getId());


                //TODO refresh
                List<String> leftList;
                //TODO optimize
                if (currentBookmark.getStatus() == BookmarkStatus.CHANGED
                        || currentBookmark.getStatus() == BookmarkStatus.UNCHANGED
                        || currentBookmark.getStatus() == BookmarkStatus.ERROR) {
                    Document doc = Jsoup
                            .parse(new File(HOME + currentBookmark.getId() + "o.html"), currentBookmark.getCharset(),
                                    currentBookmark.getUrl());
                    leftList = walkTree(doc.body(), new ArrayList<>());
                } else {
                    leftList = new ArrayList<>();
                }

                List<String> rightList;
                if (currentBookmark.getStatus() != BookmarkStatus.NEW) {
                    Document doc = Jsoup
                            .parse(new File(HOME + currentBookmark.getId() + ".html"), currentBookmark.getCharset(),
                                    currentBookmark.getUrl());
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Map<Integer, String> toIndexedMap(List<String> stringList) {
        return IntStream.range(0, stringList.size()).boxed().collect(toMap(idx -> idx, stringList::get));
    }

    private static List<String> walkTree(Element element, List<String> collection) {
        if (!isBlank(element.ownText())) {
            collection.add(element.ownText());
        }
        element.children().forEach(e -> walkTree(e, collection));
        return collection;
    }

    public void checkBookmarks() throws IOException {
        for (Bookmark bookmark : bookmarksService.getBookmarks()) {
            bookmarksService.doCheck(bookmark);
            JavaFxUtils.getController(BookmarksController.class).bookmarksTableView.refresh();
        }
        JavaFxUtils.getController(BookmarksController.class).tableView.setUserData(null);
        JavaFxUtils.getController(BookmarksController.class).webView.setUserData(null);
        //TODO refresh current bookmark if not SAME
        if (currentBookmark != null/* && JavaFxUtils.currentBookmark.getStatus() == BookmarkStatus.CHANGED*/) {
            JavaFxUtils.getController(BookmarksController.class).refresh();
        }
    }

    public void doCheck(Bookmark bookmark) throws IOException {
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

            Document doc = Jsoup
                    .parse(new File(HOME + bookmark.getId() + ".html"), bookmark.getCharset(), bookmark.getUrl());
            List<String> leftList = walkTree(doc.body(), new ArrayList<>());

            doc = Jsoup.parse(tempFile, null, bookmark.getUrl());
            List<String> rightList = walkTree(doc.body(), new ArrayList<>());

            Map<Integer, String> leftMap = toIndexedMap(leftList);
            Map<Integer, String> rightMap = toIndexedMap(rightList);

            //System.out.println(leftMap);

            textList = DiffUtils.diff(leftMap, rightMap);

            BasicFileAttributes attr = Files.readAttributes(oldFile, BasicFileAttributes.class);

            String creationDate = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withZone(ZoneId.systemDefault())
                    .format(attr.creationTime().toInstant());

            JavaFxUtils.getController(BookmarksController.class).leftCol.setText("Old page (" + creationDate + ")");

            //TODO
            if (!Comparator.compare(textList)) {
                System.out.println("changed");
                //TODO notify
                //JavaFxUtils.showAlert("Changed page: " + bookmark.getTitle(), "The page was changed", bookmark.getUrl(), AlertType.INFORMATION);
                Files.move(Paths.get(HOME + bookmark.getId() + ".html"), Paths.get(HOME + bookmark.getId() + "o.html"),
                        StandardCopyOption.REPLACE_EXISTING);
                Files.move(tempFile.toPath(), Paths.get(HOME + bookmark.getId() + ".html"),
                        StandardCopyOption.REPLACE_EXISTING);
                //TODO update date, and status in db
                bookmark.setDate(new Date());
                bookmark.setStatus(BookmarkStatus.CHANGED);
            } else {
                if (bookmark.getStatus() == BookmarkStatus.INITIALIZED) {
                    Files.copy(Paths.get(HOME + bookmark.getId() + ".html"),
                            Paths.get(HOME + bookmark.getId() + "o.html"), StandardCopyOption.REPLACE_EXISTING);
                }
                if (bookmark.getStatus() != BookmarkStatus.UNCHANGED) {
                    bookmark.setStatus(BookmarkStatus.UNCHANGED);
                    //TODO update date, and status in db
                }
                System.out.println("unchanged");
            }
        }
    }

    private String savePage(String name, String url) throws IOException {
        Connection conn = Jsoup.connect(url).ignoreContentType(true).method(Connection.Method.GET);
        Connection.Response response = conn.execute();
        byte[] raw = response.bodyAsBytes();
        Path path = Paths.get(HOME + name + ".html");
        Files.write(path, raw);
        return response.charset();
    }

    private File savePageToTmp(String url) throws IOException {
        Connection conn = Jsoup.connect(url).ignoreContentType(true).method(Connection.Method.GET);
        Connection.Response response = conn.execute();
        byte[] raw = response.bodyAsBytes();
        File tempFile = File.createTempFile("jsw-", "", null);
        FileOutputStream fos = new FileOutputStream(tempFile);
        fos.write(raw);
        return tempFile;
    }
}
