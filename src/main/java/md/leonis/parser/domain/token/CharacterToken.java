package md.leonis.parser.domain.token;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
// comment and character tokens have data.
public class CharacterToken implements Token {

    private String data;

    private TokenType type;

    public CharacterToken(char data) {
        this.data = String.valueOf(data);
        type = TokenType.CHARACTER;
    }

    public CharacterToken(StringBuilder temporaryBuffer) {
        this.data = temporaryBuffer.toString();
        type = TokenType.CHARACTER;
    }
}
