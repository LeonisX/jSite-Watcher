package md.leonis.parser.domain.token;

import java.util.List;
import lombok.Data;

@Data
public class NamedCharacter {

 private List<Integer> codepoints;
 private String characters;

}
