package md.leonis.parser.domain.token;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
// Start and end tag tokens have a tag name, a self-closing flag, and a list of attributes,
// each of which has a name and a value. When a start or end tag token is created, its self-closing flag
// must be unset (its other state is that it be set), and its attributes list must be empty.
public class EndTagToken extends TagToken {

    public EndTagToken(String name) {
        super(name, TokenType.END_TAG);
    }
}
