package md.leonis.watcher;

import javafx.application.Application;
import javafx.stage.Stage;
import md.leonis.watcher.config.Config;
import md.leonis.watcher.util.JavaFxUtils;

public class MainApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Config.loadProperties();
        //Config.loadProtectedProperties();
        JavaFxUtils.showMainPane(primaryStage);
    }

}
