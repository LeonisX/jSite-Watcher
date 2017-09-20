package md.leonis.watcher.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import md.leonis.watcher.MainApp;
import md.leonis.watcher.config.Config;
import md.leonis.watcher.service.BookmarksService;
import md.leonis.watcher.view.BookmarksController;
import md.leonis.watcher.view.MainStageController;

public class JavaFxUtils {

    private static BorderPane rootLayout;

    public static BookmarksService bookmarksService;

    private static Map<Class, Object> controllers = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static <T> T getController(Class<T> clazz) {
        return (T) controllers.get(clazz);
    }

    public static void registerController(Object object) {
        controllers.put(object.getClass(), object);
    }

    public static void showMainPane(Stage primaryStage, BookmarksService bookmarksService) {
        primaryStage.setTitle("TiVi Admin Panel");
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource(Config.resourcePath + "MainStage.fxml"));
            //loader.setController(getController(MainStageController.class));
            rootLayout = loader.load();
            MainStageController controller = loader.getController();
            controller.setBookmarksService(bookmarksService);
            Scene scene = new Scene(rootLayout, 1280, 960);
            primaryStage.setScene(scene);

            //showVoidPanel();
            showPane("Bookmarks.fxml", getController(BookmarksController.class));

            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void showPane(String resource) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource(Config.resourcePath + resource));
            Parent parent = loader.load();
            Object controller = loader.getController();
            if (controller instanceof SubPane) ((SubPane) controller).init();
            rootLayout.setCenter(parent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static <T> void showPane(String resource, T controller) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource(Config.resourcePath + resource));
            Parent parent = loader.load();
            controller = loader.getController();
            if (controller instanceof SubPane) ((SubPane) controller).init();
            rootLayout.setCenter(parent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void showVoidPanel() {
        showPane("void.fxml");
    }



    public static void showWindow(String resource, String title) {
        showWindow(resource, null, title);
    }


    public static <T> void showWindow(String resource, T controller, String title) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource(Config.resourcePath + resource));
            loader.setController(controller);
            Parent root = loader.load();
            if (controller instanceof SubPane) ((SubPane) controller).init();
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root, 640, 480));
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void showAlert(String title, String header, String text, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(text);
        alert.showAndWait();
    }

}