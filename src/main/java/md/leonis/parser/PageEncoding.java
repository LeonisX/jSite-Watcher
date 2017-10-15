package md.leonis.parser;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PageEncoding {

    private String encoding;

    private Confidence confidence;

}
