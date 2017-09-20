package md.leonis.watcher.domain;

import com.iciql.Iciql.IQColumn;
import com.iciql.Iciql.IQView;
import java.time.LocalDateTime;
import java.util.Date;
import javafx.beans.property.SimpleStringProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Bookmark {

    @IQColumn(primaryKey = true, autoIncrement = true)
    int id;

    @IQColumn(nullable = false)
    int categoryId;

    @IQColumn(nullable = false)
    String url;

    @IQColumn(nullable = false)
    String title;

    Date date;

    String settings;

}
