package md.leonis.parser.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Attribute {

    private String name;
    //TODO optionally originalName
    private String value;

}
