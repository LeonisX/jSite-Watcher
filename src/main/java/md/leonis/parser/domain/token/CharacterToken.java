package md.leonis.parser.domain.token;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
// comment and character tokens have data.
public class CharacterToken implements Token {

    private char data;

    private TokenType type;

    public CharacterToken(char data) {
        this.data = data;
        type = TokenType.CHARACTER;
    }
}
