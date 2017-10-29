package md.leonis.parser;

import lombok.AllArgsConstructor;
import md.leonis.parser.domain.token.Token;
import org.w3c.dom.Document;

import java.util.List;

@AllArgsConstructor
class TreeConstructor {

    private List<Token> tokens;

    Document constructDomTree() {
        return null;
    }
}
