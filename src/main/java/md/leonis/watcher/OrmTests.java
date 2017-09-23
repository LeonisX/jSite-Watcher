package md.leonis.watcher;

import com.iciql.Db;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import md.leonis.watcher.domain.Bookmark;
import md.leonis.watcher.domain.Category;
import md.leonis.watcher.domain.Rule;
import md.leonis.watcher.domain.RuleType;

public class OrmTests {

    public static void main(String[] args) {
        try (Db db = Db.open("jdbc:h2:mem:iciql")) {

            Rule rule = new Rule(null, 1, RuleType.EXCLUDE_BY_CLASS, "hero");

            List<Rule> rules = Arrays.asList(rule);

            db.insertAll(rules);

            List<Bookmark> bookmarkList = new ArrayList<>();
            bookmarkList.add(new Bookmark(null, 1, "http", "title", new Date(), "", rules));
            bookmarkList.add(new Bookmark(null, 2, "http2", "title2", new Date(), "2", rules));
            bookmarkList.add(new Bookmark(null, 1, "http", "title", new Date(), "", rules));
            db.insertAll(bookmarkList);
            Bookmark b = new Bookmark();
            List<Bookmark> brestock = db.from(b).where(b.getCategoryId()).is(1).select();
            List<Bookmark> ball = db.executeQuery(Bookmark.class, "select * from bookmark");
            List<Bookmark> bnames = db.from(b).selectDistinct(b.getTitle());

            Rule r = new Rule();
            List<Rule> allRules = db.from(r).where(r.getId()).is(1).select();

            ball.forEach(bk -> bk.setRules(db.from(r).where(r.getId()).is(bk.getId()).select()));

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
