package md.leonis.watcher.service;

import com.iciql.Db;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;
import md.leonis.watcher.domain.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public class BookmarksService {

    private Db db;

    private Rule rule = new Rule(1, 1, RuleType.EXCLUDE_BY_CLASS, "hero");


    public static Bookmark currentBookmark;

    public static List<TextDiff> textList = new ArrayList<>();

    public static ObservableList<TextDiff> list = FXCollections.observableList(textList);

    private List<Rule> rules = Arrays.asList(rule);

    private List<Bookmark> bookmarks = new ArrayList<>(Arrays.asList(
            new Bookmark(1, 1, "http://tv-games.ru", "TiVi", null, BookmarkStatus.NEW, "", "", rules)/*,
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

}
