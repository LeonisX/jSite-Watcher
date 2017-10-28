package md.leonis.parser.domain.token;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
// DOCTYPE tokens have a name, a public identifier, a system identifier, and a force-quirks flag.
// When a DOCTYPE token is created, its name, public identifier, and system identifier must be marked as missing
// (which is a distinct state from the empty string), and the force-quirks flag
// must be set to off (its other state is on).
public class DoctypeToken implements Token {

    private String name;
    private String publicIdentifier;
    private String systemIdentifier;
    private boolean forceQuirks;

    private TokenType type;

    public DoctypeToken() {
        name = null;
        publicIdentifier = null;
        systemIdentifier = null;
        forceQuirks = false;
        type = TokenType.DOCTYPE;
    }
}
