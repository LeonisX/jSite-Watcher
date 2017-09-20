package md.leonis.watcher.domain;

import com.iciql.Iciql.IQColumn;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    @IQColumn(primaryKey = true, autoIncrement = true)
    int id;

    @IQColumn(nullable = false)
    int parentId;

    @IQColumn(nullable = false)
    String title;

    @IQColumn
    String settings;

}
