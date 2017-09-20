package md.leonis.watcher;

import com.iciql.Db;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import md.leonis.watcher.domain.Bookmark;
import md.leonis.watcher.domain.Category;

public class OrmTests {

    public static void main(String[] args) {
        try (Db db = Db.open("jdbc:h2:mem:iciql")) {

            List<Bookmark> bookmarkList = new ArrayList<>();
            bookmarkList.add(new Bookmark(0, 1, "http", "title", new Date(), ""));
            bookmarkList.add(new Bookmark(0, 2, "http2", "title2", new Date(), "2"));
            bookmarkList.add(new Bookmark(0, 1, "http", "title", new Date(), ""));
            db.insertAll(bookmarkList);
            Bookmark b = new Bookmark();
            List<Bookmark> brestock = db.from(b).where(b.getCategoryId()).is(1).select();
            List<Bookmark> ball = db.executeQuery(Bookmark.class, "select * from bookmark");
            List<Bookmark> bnames = db.from(b).selectDistinct(b.getTitle());

            List<Category> categoryList = new ArrayList<>();
            categoryList.add(new Category(0, 1, "title", ""));
            categoryList.add(new Category(0, 2, "title2", "2"));
            db.insertAll(categoryList);
            Category c = new Category();
            List<Category> crestock = db.from(c).where(c.getParentId()).is(1).select();
            List<Category> call = db.executeQuery(Category.class, "select * from category");
            List<Category> cnames = db.from(c).selectDistinct(c.getTitle());

            db.close();
            System.out.println("i");
        }
    }
}
