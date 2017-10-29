package md.leonis.parser.domain.token;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import md.leonis.parser.domain.Attribute;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Getter
@Setter
@ToString
// Start and end tag tokens have a tag name, a self-closing flag, and a list of attributes,
// each of which has a name and a value. When a start or end tag token is created, its self-closing flag
// must be unset (its other state is that it be set), and its attributes list must be empty.
public class TagToken implements Token {

    private String name;
    //TODO optionally originalName
    private boolean selfClosing;
    private List<Attribute> attributes;

    private TokenType type;

    TagToken(String name, TokenType type) {
        this.name = name;
        this.selfClosing = false;
        this.attributes = new ArrayList<>();
        this.type = type;
    }

    // When the user agent leaves the attribute name state (and before emitting the tag token,
    // if appropriate), the complete attribute's name must be compared to the other attributes
    // on the same token; if there is already an attribute on the token with the exact same name,
    // then this is a duplicate-attribute parse error and the new attribute must be removed from the token.
    public void addAttribute(Attribute attribute) {
        if (attribute == null) {
            return;
        }
        if (attributes.contains(attribute)) {
            // This error occurs if the parser encounters an attribute in a tag that already
            // has an attribute with the same name. The parser ignores all such duplicate
            // occurrences of the attribute.
            log.error("duplicate-attribute");
        } else {
            attributes.add(attribute);
        }
    }
}
