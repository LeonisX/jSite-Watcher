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
    private Integer id;

    @IQColumn(nullable = false)
    private Integer categoryId;

    @IQColumn(nullable = false)
    private String url;

    //todo length???
    @IQColumn(nullable = false)
    private String title;

    @IQColumn
    private Date date;

    @IQColumn
    private BookmarkStatus status = BookmarkStatus.NEW;

    //todo length
    @IQColumn
    private String charset;

    @IQIgnore
    private String settings;

    @IQIgnore
    private List<Rule> rules = new ArrayList<>();

}
