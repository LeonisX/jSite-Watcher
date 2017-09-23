package md.leonis.watcher.domain;

import com.iciql.Iciql.IQColumn;
import com.iciql.Iciql.IQTable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@IQTable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Rule {

    @IQColumn(primaryKey = true, autoIncrement = true)
    Integer id;

    @IQColumn(nullable = false)
    private Integer bookmarkId;

    @IQColumn(nullable = false)
    private RuleType type;

    @IQColumn(nullable = false)
    private String value;

}
