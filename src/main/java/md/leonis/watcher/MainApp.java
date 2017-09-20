package md.leonis.watcher;

import javafx.application.Application;
import javafx.stage.Stage;
import md.leonis.watcher.config.Config;
import md.leonis.watcher.service.BookmarksService;
import md.leonis.watcher.service.DbService;
import md.leonis.watcher.utils.JavaFxUtils;

public class MainApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Config.loadProperties();

        //Config.loadProtectedProperties();

        DbService dbService = new DbService();
        BookmarksService bookmarksService = new BookmarksService(dbService);

        JavaFxUtils.bookmarksService = bookmarksService;

        JavaFxUtils.showMainPane(primaryStage, bookmarksService);
    }

}
