package md.leonis.watcher;

import com.iciql.Db;
import java.util.ArrayList;
import java.util.List;
import md.leonis.watcher.domain.PageText;

public class OrmTests {

    public static void main(String[] args) {
        try (Db db = Db.open("jdbc:h2:mem:iciql")) {
            List<PageText> pageTextList = new ArrayList<>();
            pageTextList.add(new PageText("a", "b"));
            pageTextList.add(new PageText("as", "bd"));
            db.insertAll(pageTextList);
            PageText p = new PageText();
            List<PageText> restock = db.from(p).where(p.getLeft()).is("a").select();
            List<PageText> all = db.executeQuery(PageText.class, "select * from pagetext");
            List<String> names = db.from(p).selectDistinct(p.getLeft());
            System.out.println("i");
        }
    }
}
