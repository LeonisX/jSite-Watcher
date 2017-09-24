package md.leonis.watcher.config;

import md.leonis.watcher.domain.Category;
import md.leonis.watcher.view.MainStageController;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.util.*;

public class Config {

    private Config() {
        // empty
    }

    public static List<Category> categories = new ArrayList<>(Arrays.asList(new Category(0, 1, "title", "")));

    public static String HOME = System.getProperty("user.home") + FileSystems.getDefault().getSeparator();


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
