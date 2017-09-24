package md.leonis.watcher.domain;

import com.iciql.Iciql.IQColumn;
import com.iciql.Iciql.IQIgnore;
import com.iciql.Iciql.IQTable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@IQTable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Bookmark {

    @IQColumn(primaryKey = true, autoIncrement = true)
    Integer id;

    @IQColumn(nullable = false)
    Integer categoryId;

    @IQColumn(nullable = false)
    String url;

    //todo length???
    @IQColumn(nullable = false)
    String title;

    @IQColumn
    Date date;

    //todo length
    @IQColumn
    String charset;

    @IQIgnore
    String settings;

    @IQIgnore
    List<Rule> rules = new ArrayList<>();

}
