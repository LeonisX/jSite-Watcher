package md.leonis.watcher.service;

import static java.util.stream.Collectors.toMap;
import static md.leonis.watcher.config.Config.HOME;
import static md.leonis.watcher.utils.JavaFxUtils.bookmarksService;

import com.iciql.Db;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.scene.control.TableView;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import lombok.Getter;
import md.leonis.watcher.domain.Bookmark;
import md.leonis.watcher.domain.BookmarkStatus;
import md.leonis.watcher.domain.DiffStatus;
import md.leonis.watcher.domain.Rule;
import md.leonis.watcher.domain.RuleType;
import md.leonis.watcher.domain.TextDiff;
import md.leonis.watcher.utils.Comparator;
import md.leonis.watcher.utils.DiffUtils;
import md.leonis.watcher.utils.DocumentWrapper;
import md.leonis.watcher.utils.JavaFxUtils;
import md.leonis.watcher.view.BookmarksController;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.w3c.dom.NodeList;

@Getter
public class BookmarksService {

    private Db db;

    private Rule rule = new Rule(1, 1, RuleType.EXCLUDE_BY_CLASS, "hero");

    public static Bookmark currentBookmark;

    public static List<TextDiff> textList = new ArrayList<>();

    public static ObservableList<TextDiff> list = FXCollections.observableList(textList);

    private List<Rule> rules = Arrays.asList(rule);

    private List<Bookmark> bookmarks = new ArrayList<>(
            Arrays.asList(new Bookmark(1, 1, "http://tv-games.ru", "TiVi", null, BookmarkStatus.NEW, "", "", rules)/*,
                    new Bookmark(4, 1, "http://www.emu-land.net", "mumuland", null, BookmarkStatus.NEW, "", "",
                            new ArrayList<>()*/)/*,
            new Bookmark(2, 1, "http://yandex.ru", "Yasha", null, BookmarkStatus.NEW, "", "", new ArrayList<>()),
            new Bookmark(3, 1, "http://google.com", "Grisha", null, BookmarkStatus.NEW, "", "", new ArrayList<>()),
            new Bookmark(4, 1, "http://www.emu-land.net", "mumuland", null, BookmarkStatus.NEW, "", "", new ArrayList<>()),
            new Bookmark(5, 1, "http://www.emu-russia.net", "emurasha", null, BookmarkStatus.NEW, "", "", new ArrayList<>())*/);

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
                        //System.out.println(webEngine.getDocument().getChildNodes().item(0).getClass().getSimpleName());
                        //System.out.println(webEngine.getDocument().getChildNodes().item(1).getClass().getSimpleName());
                        //HTMLDocumentImpl htmlDocument = (HTMLDocumentImpl) webEngine.getDocument();
                        //htmlDocument.caretRangeFromPoint(0, 1600).deleteContents();
                        //htmlDocument.createRange().deleteContents();
                        //htmlDocument.setDocumentURI("http://nashe.ru");
                        //System.out.println(htmlDocument.getDocumentURI());
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
        org.w3c.dom.Document document = webEngine.getDocument();

        NodeList nodeList = document.getElementsByTagName(tag);
        //TODO process tree, find href, src
        for (int i = 0; i < nodeList.getLength(); i++) {
            org.w3c.dom.Element element = (org.w3c.dom.Element) nodeList.item(i);
            if (element.hasAttribute(attribute)) {
                String attr = element.getAttribute(attribute);
                if (attr.startsWith("//")) {
                    attr = "http:" + attr;
                    element.setAttribute(attribute, attr);
                }
                if (attr.startsWith("/")) {
                    //TODO correct path (i. .e, remove page name)
                    attr = currentBookmark.getUrl() + attr;
                    element.setAttribute(attribute, attr);
                }
            }
        }
    }

    private void highlight(String[] body, String text) {
        body[0] = body[0].replace(text, "<b style='color: red; background-color: yellow'>" + text + "</b>");
    }

    public void refreshTableView(TableView<TextDiff> tableView) {
        if (tableView.getUserData() == null || !tableView.getUserData().equals(currentBookmark.getId())) {
            tableView.setUserData(currentBookmark.getId());

            if (currentBookmark.getStatus() == BookmarkStatus.CHANGED
                    || currentBookmark.getStatus() == BookmarkStatus.UNCHANGED
                    || currentBookmark.getStatus() == BookmarkStatus.ERROR) {
                WebEngine webEngine = new WebEngine();
                webEngine.getLoadWorker().stateProperty().addListener(new LeftTableListener(tableView, webEngine));
                webEngine.load(new File(HOME + currentBookmark.getId() + "o.html").toURI().toString());
            } else {
                loadRightPart(new ArrayList<>(), tableView);
            }
        }
    }

    private class LeftTableListener implements ChangeListener<State> {
        private TableView<TextDiff> tableView;
        private WebEngine webEngine;

        LeftTableListener(TableView<TextDiff> tableView, WebEngine webEngine) {
            this.tableView = tableView;
            this.webEngine = webEngine;
        }

        @Override
        public void changed(ObservableValue<? extends State> obs, State oldState, State newState) {
            if (newState == Worker.State.SUCCEEDED) {
                DocumentWrapper documentWrapper = DocumentWrapper.wrapDocument(webEngine.getDocument());
                List<String> leftList = documentWrapper.walkDocument();
                loadRightPart(leftList, tableView);
            }
        }
    }

    private void loadRightPart(List<String> leftList, TableView<TextDiff> tableView) {
        if (currentBookmark.getStatus() != BookmarkStatus.NEW) {
            WebEngine webEngine = new WebEngine();
            webEngine.getLoadWorker().stateProperty().addListener(new RightTableListener(tableView, leftList, webEngine));
            webEngine.load(new File(HOME + currentBookmark.getId() + "o.html").toURI().toString());
        } else {
            process(leftList, new ArrayList<>(), tableView);
        }
    }

    private class RightTableListener implements ChangeListener<State> {
        private List<String> leftList;
        private TableView<TextDiff> tableView;
        private WebEngine webEngine;

        RightTableListener(TableView<TextDiff> tableView, List<String> leftList, WebEngine webEngine) {
            this.tableView = tableView;
            this.leftList = leftList;
            this.webEngine = webEngine;
        }

        @Override
        public void changed(ObservableValue<? extends State> obs, State oldState, State newState) {
            if (newState == Worker.State.SUCCEEDED) {
                    // new page has loaded, process:\
                    DocumentWrapper documentWrapper = DocumentWrapper.wrapDocument(webEngine.getDocument());
                    List<String> rightList = documentWrapper.walkDocument();
                    process(leftList, rightList, tableView);
            }
        }
    }


    private void process(List<String> leftList, List<String> rightList, TableView<TextDiff> tableView) {
        Map<Integer, String> leftMap = toIndexedMap(leftList);
        Map<Integer, String> rightMap = toIndexedMap(rightList);
        textList = DiffUtils.diff(leftMap, rightMap);
        list = FXCollections.observableList(textList);
        tableView.setItems(list);
    }

    private static Map<Integer, String> toIndexedMap(List<String> stringList) {
        return IntStream.range(0, stringList.size()).boxed().collect(toMap(idx -> idx, stringList::get));
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
            WebEngine webEngine = new WebEngine();
            webEngine.getLoadWorker().stateProperty().addListener(new LeftListener(bookmark, webEngine));
            System.out.println(HOME + bookmark.getId() + ".html");
            webEngine.load(new File(HOME + bookmark.getId() + ".html").toURI().toString());
        }
    }

    private class LeftListener implements ChangeListener<State> {
        private Bookmark bookmark;
        private WebEngine webEngine;

        LeftListener(Bookmark bookmark, WebEngine webEngine) {
            this.bookmark = bookmark;
            this.webEngine = webEngine;
        }

        @Override
        public void changed(ObservableValue<? extends State> obs, State oldState, State newState) {
            System.out.println("In loadLeftPart: " + newState);
            if (newState == Worker.State.SUCCEEDED) {
                webEngine.getLoadWorker().stateProperty().removeListener(this);
                DocumentWrapper documentWrapper = DocumentWrapper.wrapDocument(webEngine.getDocument());
                List<String> leftList = documentWrapper.walkDocument();
                loadRightPart(leftList, bookmark);
            }
        }
    }

    private void loadRightPart(List<String> leftList, Bookmark bookmark) {
        System.out.println("In loadRightPart");
        File tempFile = null;
        try {
            tempFile = savePageToTmp(bookmark.getUrl());
        } catch (IOException e) {
            e.printStackTrace();
        }
        WebEngine webEngine = new WebEngine();
        webEngine.getLoadWorker().stateProperty().addListener(new RightListener(leftList, bookmark, tempFile, webEngine));
        System.out.println(tempFile.toURI().toString());
        webEngine.load(tempFile.toURI().toString());
    }

    private class RightListener implements ChangeListener<State> {
        private List<String> leftList;
        private Bookmark bookmark;
        private File tempFile;
        private WebEngine webEngine;

        public RightListener(List<String> leftList, Bookmark bookmark, File tempFile, WebEngine webEngine) {
            this.leftList = leftList;
            this.bookmark = bookmark;
            this.tempFile = tempFile;
            this.webEngine = webEngine;
        }

        @Override
        public void changed(ObservableValue<? extends State> obs, State oldState, State newState) {
            System.out.println("In loadRightPart: " + newState);
            if (newState == Worker.State.SUCCEEDED) {
                // new page has loaded, process:\
                DocumentWrapper documentWrapper = DocumentWrapper.wrapDocument(webEngine.getDocument());
                List<String> rightList = documentWrapper.walkDocument();
                BookmarksService.this.process(leftList, rightList, bookmark, tempFile);
            }
        }
    }


    private void process(List<String> leftList, List<String> rightList, Bookmark bookmark, File tempFile) {
        System.out.println("In process");
        try {
            Map<Integer, String> leftMap = toIndexedMap(leftList);
            Map<Integer, String> rightMap = toIndexedMap(rightList);

            //System.out.println(leftMap);

            textList = DiffUtils.diff(leftMap, rightMap);

            Path oldFile = Paths.get(HOME + bookmark.getId() + ".html");

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
        } catch (IOException e) {
            e.printStackTrace();
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
