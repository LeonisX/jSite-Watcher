package md.leonis.watcher.service;

import com.iciql.Db;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;
import md.leonis.watcher.domain.Bookmark;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public class BookmarksService {

    private Db db;

    private List<Bookmark> bookmarks = new ArrayList<>(Arrays.asList(
            new Bookmark(1, 1, "http://tv-games.ru", "TiVi", null, "")/*,
            new Bookmark(2, 1, "http://yandex.ru", "Yasha", null, ""),
            new Bookmark(3, 1, "http://google.com", "Grisha", null, "")*/));

    private ObservableList<Bookmark> bookmarkObservableList = FXCollections.observableArrayList(bookmarks);

    public BookmarksService(DbService dbService) {
        this.db = dbService.getDb();
    }

    public void addBookmark(String title, String url) throws IOException {
        Bookmark bookmark = new Bookmark(0, 0, url, title, null, "");
        long id = db.insertAndGetKey(bookmark);
        bookmark.setId((int) id);
        bookmarkObservableList.add(bookmark);
    }

}
