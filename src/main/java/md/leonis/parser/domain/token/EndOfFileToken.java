package md.leonis.parser.domain.token;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class EndOfFileToken implements Token {

    private TokenType type = TokenType.END_OF_FILE;

}
