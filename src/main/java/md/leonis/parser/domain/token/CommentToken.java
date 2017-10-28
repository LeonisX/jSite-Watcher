package md.leonis.parser.domain.token;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
// comment and character tokens have data.
public class CommentToken implements Token {

    private String data;

    private TokenType type;

    public CommentToken(String data) {
        this.data = data;
        type = TokenType.COMMENT;
    }
}
