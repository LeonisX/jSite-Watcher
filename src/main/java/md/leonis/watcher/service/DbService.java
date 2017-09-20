package md.leonis.watcher.service;

import com.iciql.Db;
import lombok.Getter;

@Getter
public class DbService {

    private Db db;

    public DbService() {
        db = Db.open("jdbc:h2:mem:iciql");
    }

}
