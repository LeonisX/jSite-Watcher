package md.leonis.watcher.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TextDiff {

    private Integer leftIndex;
    private String leftText;

    private Integer rightIndex;
    private String rightText;

    private DiffStatus status;

}
