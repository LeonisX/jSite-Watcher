package md.leonis.watcher.view.frame;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import md.leonis.watcher.config.Config;
import md.leonis.watcher.domain.Bookmark;
import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Jsoup;

public class AddBookmarkController {

    @FXML
    public TextField titleTextField;

    @FXML
    public TextField urlTextField;

    @FXML
    private void initialize() {

    }

    public void addBookmark() throws IOException {
        Config.addBookmark(titleTextField.getText(), urlTextField.getText());
        close();
    }

    public void close() {
        ((Stage) titleTextField.getScene().getWindow()).close();
    }
}