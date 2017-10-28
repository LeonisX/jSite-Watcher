package md.leonis.parser.domain.token;

// The output of the tokenization step is a series of zero or more of the following tokens:
// DOCTYPE, start tag, end tag, comment, character, end-of-file.
public interface Token {

    TokenType getType();

}
