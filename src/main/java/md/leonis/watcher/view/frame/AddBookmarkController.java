package md.leonis.watcher.view.frame;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import md.leonis.watcher.utils.JavaFxUtils;
import md.leonis.watcher.view.BookmarksController;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

import static md.leonis.watcher.utils.JavaFxUtils.bookmarksService;
import static md.leonis.watcher.utils.JavaFxUtils.getController;
import static md.leonis.watcher.utils.JavaFxUtils.registerController;

public class AddBookmarkController {

    @FXML
    private TextField titleTextField;

    @FXML
    private TextField urlTextField;

    @FXML
    private HBox titleHBox;

    @FXML
    private HBox buttonHBox;

    @FXML
    private void initialize() {
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        if (clipboard.hasString() && clipboard.getString().startsWith("http")) {
            urlTextField.setText(clipboard.getString());
            clipboard.clear();
        }

        titleHBox.managedProperty().bind(titleHBox.visibleProperty());
        buttonHBox.managedProperty().bind(buttonHBox.visibleProperty());
        registerController(this);
    }

    public void addBookmark() throws IOException {
        bookmarksService.addBookmark(titleTextField.getText(), urlTextField.getText());
        close();
    }

    public void testButtonClick() {
        try {
            if (!urlTextField.getText().startsWith("http")) {
                urlTextField.setText("http://" + urlTextField.getText());
            }
            Connection connection = Jsoup.connect(urlTextField.getText());
            Connection.Response response = connection.execute();
            Document document = response.parse();
            getController(BookmarksController.class).webView.getEngine().loadContent(response.body());
            titleTextField.setText(document.title());
            buttonHBox.setVisible(false);
            titleHBox.setVisible(true);
        } catch (Exception e) {
            JavaFxUtils.showAlert("Error", "Can't open url :(", e.getMessage(), Alert.AlertType.ERROR);
        }

    }

    public void close() {
        ((Stage) titleTextField.getScene().getWindow()).close();
    }
}
