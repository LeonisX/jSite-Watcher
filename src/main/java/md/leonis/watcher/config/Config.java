package md.leonis.watcher.config;

import com.iciql.Db;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import md.leonis.watcher.domain.Bookmark;
import md.leonis.watcher.domain.Category;
import md.leonis.watcher.view.MainStageController;
import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Jsoup;

public class Config {

    private static Db db = Db.open("jdbc:h2:mem:iciql");

    public static List<Category> categories = new ArrayList<>(Arrays.asList(new Category(0, 1, "title", "")));

    public static List<Bookmark> bookmarks = new ArrayList<>(Arrays.asList(
            new Bookmark(1, 1, "http://tv-games.ru", "TiVi", null, "")/*,
            new Bookmark(2, 1, "http://yandex.ru", "Yasha", null, ""),
            new Bookmark(3, 1, "http://google.com", "Grisha", null, "")*/));

    public static ObservableList<Bookmark> bookmarkObservableList = FXCollections.observableArrayList(bookmarks);

    public static void addBookmark(String title, String url) throws IOException {
        Bookmark bookmark = new Bookmark(0, 0, url, title, null, "");
        long id = db.insertAndGetKey(bookmark);
        bookmark.setId((int) id);
        bookmarkObservableList.add(bookmark);

    }


    static String apiPath;
    public static String sitePath;
    static String sampleVideo;

    static String serverSecret;
    public static String testDbPassword;

    public static final String resourcePath = "/" + MainStageController.class.getPackage().getName().replaceAll("\\.", "/") + "/";

    public static void loadProperties() throws IOException {
        try (InputStream inputStream = Config.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (inputStream == null) throw new FileNotFoundException("Property file not found...");
            Properties prop = new Properties();
            prop.load(inputStream);
            apiPath = prop.getProperty("api.path");
            sitePath = prop.getProperty("site.path");
            sampleVideo = prop.getProperty("sample.video");
        }
    }

    public static void loadProtectedProperties() throws IOException {
        try (InputStream inputStream = Config.class.getClassLoader().getResourceAsStream("protected.properties")) {
            Properties prop = new Properties();
            prop.load(inputStream);
            serverSecret = prop.getProperty("server.secret");
            testDbPassword = prop.getProperty("test.db.password");
        }
    }
}