package md.leonis.parser;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import md.leonis.parser.domain.Attribute;
import md.leonis.parser.domain.PageEncoding;
import md.leonis.parser.domain.State;
import md.leonis.parser.domain.token.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Getter
@Setter
class Tokenizer {

    private String encoding;
    private PageEncoding pageEncoding;
    private byte[] html;
    private String htmlString;
    private int length;
    private int position;

    private State state;
    private State returnState;
    private List<Token> tokens;
    private TagToken tagToken;
    private Attribute attribute;
    private CommentToken commentToken;
    private StringBuilder temporaryBuffer;

    Tokenizer(String htmlString) {
        this.htmlString = htmlString;
        position = 0;
        state = State.DATA;
        tokens = new ArrayList<>();
    }

    List<Token> tokenize() {
        length = htmlString.length();

        //The `next input character` is the first character in the input stream that has not yet been `consumed`
        // or explicitly ignored by the requirements in this section. Initially, the next input character
        // is the first character in the input. The `current input character` is the last character to have been consumed.

        //The `insertion point` is the position (just before a character or just before the end of the input stream)
        // where content inserted using document.write() is actually inserted. The insertion point is relative to
        // the position of the character immediately after it, it is not an absolute offset into the input stream.
        // Initially, the insertion point is undefined.

        //The "EOF" character in the tables below is a conceptual character representing the end of the input stream.
        // If the parser is a script-created parser, then the end of the input stream is reached when an
        // `explicit "EOF" character` (inserted by the document.close() method) is consumed. Otherwise, the
        // "EOF" character is not a real character in the stream, but rather the lack of any further characters.

        while (position <= length) {
            switch (state) {
                case DATA:
                    inData();
                    break;
                case TAG_OPEN:
                    inTagOpen();
                    break;
                case END_TAG_OPEN:
                    inEndTagOpen();
                    break;
                case TAG_NAME:
                    inTagName();
                    break;
                case BEFORE_ATTRIBUTE_NAME:
                    inBeforeAttributeName();
                    break;
                case ATTRIBUTE_NAME:
                    inAttributeName();
                    break;
                case AFTER_ATTRIBUTE_NAME:
                    inAfterAttributeName();
                    break;
                case BEFORE_ATTRIBUTE_VALUE:
                    inBeforeAttributeValue();
                    break;
                case ATTRIBUTE_VALUE_DOUBLE_QUOTED:
                    inAttributeValueDoubleQuoted();
                    break;
                case ATTRIBUTE_VALUE_SINGLE_QUOTED:
                    inAttributeValueSingleQuoted();
                    break;
                case ATTRIBUTE_VALUE_UNQUOTED:
                    inAttributeValueUnquoted();
                    break;
                case AFTER_ATTRIBUTE_VALUE_QUOTED:
                    inAfterAttributeValue();
                    break;
                case SELF_CLOSING_START_TAG:
                    inSelfClosingStartTag();
                    break;
                case CHARACTER_REFERENCE:
                    inCharacterReferenceState();
                    break;
                default:
                    System.out.println(tokens);
                    throw new RuntimeException("" + state);
            }
        }
        return tokens;
    }

    //12.2.5.1 Data state
    private void inData() {
        // Consume the next input character:
        // EOF: Emit an end-of-file token.
        if (position == length) {
            tokens.add(new EndOfFileToken());
            position++;
            return;
        }
        switch (htmlString.charAt(position)) {
            // U+0026 AMPERSAND (&): Set the return state to the data state.
            // Switch to the character reference state.
            case '&':
                returnState = State.DATA;
                state = State.CHARACTER_REFERENCE;
                position++;
                break;
            // U+003C LESS-THAN SIGN (<): Switch to the tag open state.
            case '<':
                state = State.TAG_OPEN;
                position++;
                break;
            // U+0000 NULL: This is an unexpected-null-character parse error. Emit the current input character as a character token.
            case 0x0000:
                // This error occurs if the parser encounters a U+0000 NULL code point in the input stream
                // in certain positions. In general, such code points are either completely ignored or,
                // for security reasons, replaced with a U+FFFD REPLACEMENT CHARACTER.
                //TODO optionally after refactor
                tokens.add(new CharacterToken((char) 0xFFFD));
                //TODO pass to special class
                //TODO where optionally process exceptions and show detailed logs
                log.error("unexpected-null-character");
                position++;
                break;
            default:
                // Anything else: Emit the current input character as a character token.
                tokens.add(new CharacterToken(htmlString.charAt(position)));
                position++;
        }
    }

    //TODO 12.2.5.2 RCDATA state

    //TODO 12.2.5.3 RAWTEXT state

    //TODO 12.2.5.4 Script data state

    //TODO 12.2.5.5 PLAINTEXT state

    //12.2.5.6 Tag open state
    private void inTagOpen() {
        // Consume the next input character:
        // EOF: This is an eof-before-tag-name parse error.
        // Emit a U+003C LESS-THAN SIGN character token and an end-of-file token.
        if (position == length) {
            log.error("eof-before-tag-name");
            tokens.add(new CharacterToken((char) 0x003C));
            tokens.add(new EndOfFileToken());
            position++;
            return;
        }
        switch (htmlString.charAt(position)) {
            // U+0021 EXCLAMATION MARK (!): Switch to the markup declaration open state.
            case '!':
                state = State.MARKUP_DECLARATION_OPEN;
                position++;
                break;
            // U+002F SOLIDUS (/): Switch to the end tag open state.
            case '/':
                state = State.END_TAG_OPEN;
                position++;
                break;
            // U+003F QUESTION MARK (?): This is an unexpected-question-mark-instead-of-tag-name parse error.
            // Create a comment token whose data is the empty string. Reconsume in the bogus comment state.
            case '?':
                // This error occurs if the parser encounters a U+003F (?) code point where first code point of
                // a start tag name is expected. The U+003F (?) and all content that follows up to a U+003E (>)
                // code point (if present) or to the end of the input stream is treated as a comment.
                log.error("unexpected-question-mark-instead-of-tag-name");
                tokens.add(new CommentToken(""));
                state = State.BOGUS_COMMENT;
                break;
            default:
                // ASCII alpha: Create a new start tag token, set its tag name to the empty string.
                // Reconsume in the tag name state.
                if (Constants.ASCII_ALPHA.contains(htmlString.charAt(position))) {
                    state = State.TAG_NAME;
                    tagToken = new StartTagToken("");
                } else {
                    // Anything else: This is an invalid-first-character-of-tag-name parse error.
                    // This error occurs if the parser encounters a code point that is not an ASCII alpha
                    // where first code point of a start tag name or an end tag name is expected.
                    // If a start tag was expected such code point and a preceding U+003C (<) is treated
                    // as text content, and all content that follows is treated as markup. Whereas,
                    // if an end tag was expected, such code point and all content that follows up to
                    // a U+003E (>) code point (if present) or to the end of the input stream is treated as a comment.
                    log.error("invalid-first-character-of-tag-name");
                    // Emit a U+003C LESS-THAN SIGN character token. Reconsume in the data state.
                    tokens.add(new CharacterToken((char) 0x003C));
                    state = State.DATA;
                }
        }
    }

    //12.2.5.7 End tag open state
    private void inEndTagOpen() {
        // Consume the next input character:
        // EOF
        if (position == length) {
            // This is an eof-before-tag-name parse error.
            log.error("eof-before-tag-name");
            // Emit a U+003C LESS-THAN SIGN character token,
            tokens.add(new CharacterToken((char) 0x003C));
            // a U+002F SOLIDUS character token and an end-of-file token.
            tokens.add(new CharacterToken((char) 0x002F));
            tokens.add(new EndOfFileToken());
            position++;
            return;
        }
        char c = htmlString.charAt(position);
        switch (c) {
            // U+003E GREATER-THAN SIGN (>)
            case '>':
                //This is a missing-end-tag-name parse error. Switch to the data state.
                log.error("missing-end-tag-name");
                state = State.DATA;
                position++;
                break;
            default:
                // ASCII alpha: Create a new end tag token, set its tag name to the empty string.
                if (Constants.ASCII_ALPHA.contains(c)) {
                    tagToken = new EndTagToken("");
                    // Reconsume in the tag name state.
                    state = State.TAG_NAME;
                } else {
                    // This is an invalid-first-character-of-tag-name parse error.
                    log.error("invalid-first-character-of-tag-name");
                    // Create a comment token whose data is the empty string.
                    commentToken = new CommentToken("");
                    // Reconsume in the bogus comment state.
                    state = State.BOGUS_COMMENT;
                }
        }
    }

    //12.2.5.8 Tag name state
    private void inTagName() {
        // Consume the next input character:
        // EOF: This is an eof-in-tag parse error. Emit an end-of-file token.
        if (position == length) {
            // This error occurs if the parser encounters the end of the input stream in a start tag
            // or an end tag (e.g., <div id=). Such a tag is completely ignored.
            log.error("eof-in-tag");
            tokens.add(new EndOfFileToken());
            position++;
            return;
        }
        char c = htmlString.charAt(position);
        switch (c) {
            //U+0009 CHARACTER TABULATION (tab)
            case 0x0009:
                // U+000A LINE FEED (LF)
            case 0x000A:
                //U+000C FORM FEED (FF)
            case 0x000C:
                //U+0020 SPACE
            case 0x0020:
                //Switch to the before attribute name state.
                state = State.BEFORE_ATTRIBUTE_NAME;
                position++;
                break;
            // U+002F SOLIDUS (/)
            case '/':
                //Switch to the self-closing start tag state.
                state = State.SELF_CLOSING_START_TAG;
                position++;
                break;
            // U+003E GREATER-THAN SIGN (>)
            case '>':
                //Switch to the data state. Emit the current tag token.
                state = State.DATA;
                tokens.add(tagToken);
                position++;
                break;
            // U+0000 NULL
            case 0x0000:
                // This is an unexpected-null-character parse error.
                // Append a U+FFFD REPLACEMENT CHARACTER character to the current tag token's tag name.
                state = State.DATA;
                tagToken.setName(tagToken.getName() + (char) 0xFFFD);
                position++;
                break;
            default:
                // ASCII upper alpha: Append the lowercase version of the current input character
                // (add 0x0020 to the character's code point) to the current tag token's tag name.
                if (Constants.ASCII_UPPER_ALPHA.contains(c)) {
                    tagToken.setName(tagToken.getName() + Character.toLowerCase(c));
                } else {
                    // Anything else: Append the current input character to the current tag token's tag name.
                    tagToken.setName(tagToken.getName() + c);
                }
                position++;
        }
    }

    //TODO 12.2.5.9 RCDATA less-than sign state
    //TODO 12.2.5.10 RCDATA end tag open state
    //TODO 12.2.5.11 RCDATA end tag name state
    //TODO 12.2.5.12 RAWTEXT less-than sign state
    //TODO 12.2.5.13 RAWTEXT end tag open state
    //TODO 12.2.5.14 RAWTEXT end tag name state
    //TODO 12.2.5.15 Script data less-than sign state
    //TODO 12.2.5.16 Script data end tag open state
    //TODO 12.2.5.17 Script data end tag name state
    //TODO 12.2.5.18 Script data escape start state
    //TODO 12.2.5.19 Script data escape start dash state
    //TODO 12.2.5.20 Script data escaped state
    //TODO 12.2.5.21 Script data escaped dash state
    //TODO 12.2.5.22 Script data escaped dash dash state
    //TODO 12.2.5.23 Script data escaped less-than sign state
    //TODO 12.2.5.24 Script data escaped end tag open state
    //TODO 12.2.5.25 Script data escaped end tag name state
    //TODO 12.2.5.26 Script data double escape start state
    //TODO 12.2.5.27 Script data double escaped state
    //TODO 12.2.5.28 Script data double escaped dash state
    //TODO 12.2.5.29 Script data double escaped dash dash state
    //TODO 12.2.5.30 Script data double escaped less-than sign state
    //TODO 12.2.5.31 Script data double escape end state

    //12.2.5.32 Before attribute name state
    private void inBeforeAttributeName() {
        // Consume the next input character:
        // Reconsume in the after attribute name state.
        if (position == length) {
            state = State.AFTER_ATTRIBUTE_NAME;
            return;
        }
        char c = htmlString.charAt(position);
        switch (c) {
            //U+0009 CHARACTER TABULATION (tab)
            case 0x0009:
                // U+000A LINE FEED (LF)
            case 0x000A:
                //U+000C FORM FEED (FF)
            case 0x000C:
                //U+0020 SPACE
            case 0x0020:
                //Ignore the character.
                position++;
                break;
            // U+002F SOLIDUS (/)
            case '/':
                // U+003E GREATER-THAN SIGN (>)
            case '>':
                //Reconsume in the after attribute name state.
                state = State.AFTER_ATTRIBUTE_NAME;
                break;
            // U+003D EQUALS SIGN (=)
            case '=':
                // This is an unexpected-equals-sign-before-attribute-name parse error.
                // This error occurs if the parser encounters a U+003D (=) code point
                // before an attribute name. In this case the parser treats U+003D (=)
                // as the first code point of the attribute name.
                //The common reason for this error is a forgotten attribute name.
                log.error("unexpected-equals-sign-before-attribute-name");
                // Start a new attribute in the current tag token.
                // Set that attribute's name to the current input character,
                // and its value to the empty string. Switch to the attribute name state.
                state = State.ATTRIBUTE_NAME;
                attribute = new Attribute(String.valueOf(c), "");
                position++;
                break;
            default:
                // Start a new attribute in the current tag token.
                // Set that attribute name and value to the empty string.
                // Reconsume in the attribute name state.
                state = State.ATTRIBUTE_NAME;
                attribute = new Attribute("", "");
        }
    }

    //12.2.5.33 Attribute name state
    private void inAttributeName() {
        // Consume the next input character:
        // Reconsume in the after attribute name state.
        if (position == length) {
            state = State.AFTER_ATTRIBUTE_NAME;
            return;
        }
        char c = htmlString.charAt(position);
        switch (c) {
            //U+0009 CHARACTER TABULATION (tab)
            case 0x0009:
                // U+000A LINE FEED (LF)
            case 0x000A:
                //U+000C FORM FEED (FF)
            case 0x000C:
                //U+0020 SPACE
            case 0x0020:
                // U+002F SOLIDUS (/)
            case '/':
                // U+003E GREATER-THAN SIGN (>)
            case '>':
                //Reconsume in the after attribute name state.
                state = State.AFTER_ATTRIBUTE_NAME;
                break;
            // U+003D EQUALS SIGN (=)
            case '=':
                // Switch to the before attribute value state.
                state = State.BEFORE_ATTRIBUTE_VALUE;
                position++;
                break;
            //U+0000 NULL
            case 0x0000:
                // This is an unexpected-null-character parse error.
                log.error("unexpected-null-character");
                // Append a U+FFFD REPLACEMENT CHARACTER character to the current attribute's name.
                attribute.setName(attribute.getName() + (char) 0xFFFD);
                position++;
                break;
            // U+0022 QUOTATION MARK (")
            case '"':
                //U+0027 APOSTROPHE (')
            case '\'':
                //U+003C LESS-THAN SIGN (<)
            case '<':
                // This is an unexpected-character-in-attribute-name parse error.
                log.error("unexpected-character-in-attribute-name");
                // Treat it as per the "anything else" entry below.
                attribute.setName(attribute.getName() + c);
                position++;
                break;
            default:
                if (Constants.ASCII_UPPER_ALPHA.contains(c)) {
                    // Append the lowercase version of the current input character
                    // (add 0x0020 to the character's code point) to the current attribute's name.
                    attribute.setName(attribute.getName() + Character.toLowerCase(c));
                    position++;
                    break;
                } else {
                    // Anything else: Append the current input character to the current attribute's name.
                    attribute.setName(attribute.getName() + c);
                    position++;
                    break;
                }
        }
    }

    //12.2.5.34 After attribute name state
    private void inAfterAttributeName() {
        // Consume the next input character:
        // This is an eof-in-tag parse error. Emit an end-of-file token.
        if (position == length) {
            log.error("eof-in-tag");
            tokens.add(new EndOfFileToken());
            position++;
            return;
        }
        char c = htmlString.charAt(position);
        switch (c) {
            //U+0009 CHARACTER TABULATION (tab)
            case 0x0009:
                // U+000A LINE FEED (LF)
            case 0x000A:
                //U+000C FORM FEED (FF)
            case 0x000C:
                //U+0020 SPACE
            case 0x0020:
                // Ignore the character.
                position++;
                break;
            // U+002F SOLIDUS (/)
            case '/':
                // Switch to the self-closing start tag state.
                state = State.SELF_CLOSING_START_TAG;
                tagToken.addAttribute(attribute);
                attribute = null;
                position++;
                break;
            // U+003D EQUALS SIGN (=)
            case '=':
                // Switch to the before attribute value state.
                state = State.BEFORE_ATTRIBUTE_VALUE;
                position++;
                break;
            //U+003E GREATER-THAN SIGN (>)
            case '>':
                // Switch to the data state. Emit the current tag token.
                tagToken.addAttribute(attribute);
                attribute = null;
                tokens.add(tagToken);
                state = State.DATA;
                position++;
                break;
            default:
                tagToken.addAttribute(attribute);
                attribute = null;
                // Start a new attribute in the current tag token.
                // Set that attribute name and value to the empty string.
                attribute = new Attribute("", "");
                // Reconsume in the attribute name state.
                state = State.ATTRIBUTE_NAME;
                break;
        }
    }

    //12.2.5.35 Before attribute value state
    private void inBeforeAttributeValue() {
        // Consume the next input character:
        char c = htmlString.charAt(position);
        switch (c) {
            //U+0009 CHARACTER TABULATION (tab)
            case 0x0009:
                // U+000A LINE FEED (LF)
            case 0x000A:
                //U+000C FORM FEED (FF)
            case 0x000C:
                //U+0020 SPACE
            case 0x0020:
                // Ignore the character.
                position++;
                break;
            // U+0022 QUOTATION MARK (")
            case '"':
                // Switch to the attribute value (double-quoted) state.
                state = State.ATTRIBUTE_VALUE_DOUBLE_QUOTED;
                position++;
                break;
            // U+0027 APOSTROPHE (')
            case '\'':
                // Switch to the attribute value (single-quoted) state.
                state = State.ATTRIBUTE_VALUE_SINGLE_QUOTED;
                position++;
                break;
            //U+003E GREATER-THAN SIGN (>)
            case '>':
                // This is a missing-attribute-value parse error.
                // Switch to the data state. Emit the current tag token.
                tagToken.addAttribute(attribute);
                attribute = null;
                tokens.add(tagToken);
                state = State.DATA;
                position++;
                break;
            default:
                // Reconsume in the attribute value (unquoted) state.
                state = State.ATTRIBUTE_VALUE_UNQUOTED;
                break;
        }
    }

    //12.2.5.36 Attribute value (double-quoted) state
    private void inAttributeValueDoubleQuoted() {
        // Consume the next input character:
        // This is an eof-in-tag parse error. Emit an end-of-file token.
        if (position == length) {
            log.error("eof-in-tag");
            tokens.add(new EndOfFileToken());
            position++;
            return;
        }
        char c = htmlString.charAt(position);
        switch (c) {
            // U+0022 QUOTATION MARK (")
            case '"':
                // Switch to the after attribute value (quoted) state.
                state = State.AFTER_ATTRIBUTE_VALUE_QUOTED;
                tagToken.addAttribute(attribute);
                attribute = null;
                position++;
                break;
            // U+0026 AMPERSAND (&)
            case '&':
                // Set the return state to the attribute value (double-quoted) state.
                // Switch to the character reference state.
                returnState = State.ATTRIBUTE_VALUE_DOUBLE_QUOTED;
                state = State.CHARACTER_REFERENCE;
                position++;
                break;
            // U+0000 NULL
            case 0x0000:
                // This is an unexpected-null-character parse error.
                log.error("unexpected-null-character");
                // Append a U+FFFD REPLACEMENT CHARACTER character to the current attribute's name.
                attribute.setName(attribute.getName() + (char) 0xFFFD);
                position++;
                break;
            default:
                // Anything else: Append the current input character to the current attribute's value.
                attribute.setValue(attribute.getValue() + c);
                position++;
                break;
        }
    }

    //12.2.5.37 Attribute value (single-quoted) state
    private void inAttributeValueSingleQuoted() {
        // Consume the next input character:
        // This is an eof-in-tag parse error. Emit an end-of-file token.
        if (position == length) {
            log.error("eof-in-tag");
            tokens.add(new EndOfFileToken());
            position++;
            return;
        }
        char c = htmlString.charAt(position);
        switch (c) {
            // U+0027 APOSTROPHE (')
            case '\'':
                // Switch to the after attribute value (quoted) state.
                state = State.AFTER_ATTRIBUTE_VALUE_QUOTED;
                tagToken.addAttribute(attribute);
                attribute = null;
                position++;
                break;
            // U+0026 AMPERSAND (&)
            case '&':
                // Set the return state to the attribute value (single-quoted) state.
                // Switch to the character reference state.
                returnState = State.ATTRIBUTE_VALUE_SINGLE_QUOTED;
                state = State.CHARACTER_REFERENCE;
                position++;
                break;
            // U+0000 NULL
            case 0x0000:
                // This is an unexpected-null-character parse error.
                log.error("unexpected-null-character");
                // Append a U+FFFD REPLACEMENT CHARACTER character to the current attribute's name.
                attribute.setName(attribute.getName() + (char) 0xFFFD);
                position++;
                break;
            default:
                // Anything else: Append the current input character to the current attribute's value.
                attribute.setValue(attribute.getValue() + c);
                position++;
                break;
        }
    }

    //12.2.5.38 Attribute value (unquoted) state
    private void inAttributeValueUnquoted() {
        // Consume the next input character:
        // This is an eof-in-tag parse error. Emit an end-of-file token.
        if (position == length) {
            log.error("eof-in-tag");
            tokens.add(new EndOfFileToken());
            position++;
            return;
        }
        char c = htmlString.charAt(position);
        switch (c) {
            // U+0009 CHARACTER TABULATION (tab)
            case 0x0009:
                // U+000A LINE FEED (LF)
            case 0x000A:
                //U+000C FORM FEED (FF)
            case 0x000C:
                //U+0020 SPACE
            case 0x0020:
                //Switch to the before attribute name state.
                state = State.BEFORE_ATTRIBUTE_NAME;
                tagToken.addAttribute(attribute);
                attribute = null;
                position++;
                break;
            // U+0026 AMPERSAND (&)
            case '&':
                // Set the return state to the attribute value (unquoted) state.
                // Switch to the character reference state.
                returnState = State.ATTRIBUTE_VALUE_UNQUOTED;
                state = State.CHARACTER_REFERENCE;
                position++;
                break;
            //U+003E GREATER-THAN SIGN (>)
            case '>':
                // Switch to the data state. Emit the current tag token.
                tagToken.addAttribute(attribute);
                attribute = null;
                tokens.add(tagToken);
                state = State.DATA;
                position++;
                break;
            // U+0000 NULL
            case 0x0000:
                // This is an unexpected-null-character parse error.
                log.error("unexpected-null-character");
                // Append a U+FFFD REPLACEMENT CHARACTER character to the current attribute's name.
                attribute.setName(attribute.getName() + (char) 0xFFFD);
                position++;
                break;
            // U+0022 QUOTATION MARK (")
            case '"':
                // U+0027 APOSTROPHE (')
            case '\'':
                // U+003C LESS-THAN SIGN (<)
            case '<':
                // U+003D EQUALS SIGN (=)
            case '=':
                // U+0060 GRAVE ACCENT (`)
            case '`':
                // This is an unexpected-character-in-unquoted-attribute-value parse error.
                log.error("unexpected-character-in-unquoted-attribute-value");
                // Treat it as per the "anything else" entry below.
                attribute.setValue(attribute.getValue() + c);
                position++;
                break;
            default:
                // Anything else: Append the current input character to the current attribute's value.
                attribute.setValue(attribute.getValue() + c);
                position++;
                break;
        }
    }

    //12.2.5.39 After attribute value (quoted) state
    private void inAfterAttributeValue() {
        // Consume the next input character:
        // This is an eof-in-tag parse error. Emit an end-of-file token.
        if (position == length) {
            log.error("eof-in-tag");
            tokens.add(new EndOfFileToken());
            position++;
            return;
        }
        char c = htmlString.charAt(position);
        switch (c) {
            //U+0009 CHARACTER TABULATION (tab)
            case 0x0009:
                // U+000A LINE FEED (LF)
            case 0x000A:
                //U+000C FORM FEED (FF)
            case 0x000C:
                //U+0020 SPACE
            case 0x0020:
                // Switch to the before attribute name state.
                state = State.BEFORE_ATTRIBUTE_NAME;
                tagToken.addAttribute(attribute);
                attribute = null;
                position++;
                break;
            // U+002F SOLIDUS (/)
            case '/':
                // Switch to the self-closing start tag state.
                state = State.SELF_CLOSING_START_TAG;
                tagToken.addAttribute(attribute);
                attribute = null;
                position++;
                break;
            //U+003E GREATER-THAN SIGN (>)
            case '>':
                // Switch to the data state. Emit the current tag token.
                tagToken.addAttribute(attribute);
                attribute = null;
                tokens.add(tagToken);
                state = State.DATA;
                position++;
                break;
            default:
                // This is a missing-whitespace-between-attributes parse error.
                log.error("missing-whitespace-between-attributes");
                // Reconsume in the before attribute name state.
                tagToken.addAttribute(attribute);
                attribute = null;
                state = State.BEFORE_ATTRIBUTE_NAME;
                break;
        }
    }

    //12.2.5.40 Self-closing start tag state
    private void inSelfClosingStartTag() {
        // Consume the next input character:
        // This is an eof-in-tag parse error. Emit an end-of-file token.
        if (position == length) {
            log.error("eof-in-tag");
            tokens.add(new EndOfFileToken());
            position++;
            return;
        }
        char c = htmlString.charAt(position);
        switch (c) {
            //U+003E GREATER-THAN SIGN (>)
            case '>':
                // Set the self-closing flag of the current tag token.
                // Switch to the data state. Emit the current tag token.
                tagToken.addAttribute(attribute);
                attribute = null;
                tagToken.setSelfClosing(true);
                tokens.add(tagToken);
                state = State.DATA;
                position++;
                break;
            default:
                // This is an unexpected-solidus-in-tag parse error.
                log.error("unexpected-solidus-in-tag");
                // Reconsume in the before attribute name state.
                tagToken.addAttribute(attribute);
                attribute = null;
                state = State.BEFORE_ATTRIBUTE_NAME;
                break;
        }
    }






    //TODO 12.2.5.41 Bogus comment state
    //TODO 12.2.5.42 Markup declaration open state
    //TODO 12.2.5.43 Comment start state
    //TODO 12.2.5.44 Comment start dash state
    //TODO 12.2.5.45 Comment state
    //TODO 12.2.5.46 Comment less-than sign state
    //TODO 12.2.5.47 Comment less-than sign bang state
    //TODO 12.2.5.48 Comment less-than sign bang dash state
    //TODO 12.2.5.49 Comment less-than sign bang dash dash state
    //TODO 12.2.5.50 Comment end dash state
    //TODO 12.2.5.51 Comment end state
    //TODO 12.2.5.52 Comment end bang state

    //TODO 12.2.5.53 DOCTYPE state
    //TODO 12.2.5.54 Before DOCTYPE name state
    //TODO 12.2.5.55 DOCTYPE name state
    //TODO 12.2.5.56 After DOCTYPE name state
    //TODO 12.2.5.57 After DOCTYPE public keyword state
    //TODO 12.2.5.58 Before DOCTYPE public identifier state
    //TODO 12.2.5.59 DOCTYPE public identifier (double-quoted) state
    //TODO 12.2.5.60 DOCTYPE public identifier (single-quoted) state
    //TODO 12.2.5.61 After DOCTYPE public identifier state
    //TODO 12.2.5.62 Between DOCTYPE public and system identifiers state
    //TODO 12.2.5.63 After DOCTYPE system keyword state
    //TODO 12.2.5.64 Before DOCTYPE system identifier state
    //TODO 12.2.5.65 DOCTYPE system identifier (double-quoted) state
    //TODO 12.2.5.66 DOCTYPE system identifier (single-quoted) state
    //TODO 12.2.5.67 After DOCTYPE system identifier state
    //TODO 12.2.5.68 Bogus DOCTYPE state

    //TODO 12.2.5.69 CDATA section state
    //TODO 12.2.5.70 CDATA section bracket state
    //TODO 12.2.5.71 CDATA section end state

    //12.2.5.72 Character reference state
    private void inCharacterReferenceState() {
        // Set the temporary buffer to the empty string.
        // Append a U+0026 AMPERSAND (&) character to the temporary buffer.
        temporaryBuffer = new StringBuilder("&");
        // Consume the next input character:
        char c = htmlString.charAt(position);
        switch (c) {
            // U+0023 NUMBER SIGN (#)
            case '#':
                // Append the current input character to the temporary buffer.
                temporaryBuffer.append(c);
                // Switch to the numeric character reference state.
                state = State.NUMERIC_CHARACTER_REFERENCE;
                position++;
                break;
            default:
                // ASCII alphanumeric: Reconsume in the named character reference state.
                if (Constants.ASCII_ALPHANUMERIC.contains(c)) {
                    state = State.NAMED_CHARACTER_REFERENCE;
                    break;
                } else {
                    // Anything else: Flush code points consumed as a character reference.
                    flushCodePoints();
                    // Reconsume in the return state.
                    state = returnState;
                    position++;
                    break;
                }
        }
    }

    //TODO 12.2.5.73 Named character reference state
    //TODO 12.2.5.74 Ambiguous ampersand state
    //TODO 12.2.5.75 Numeric character reference state
    //TODO 12.2.5.76 Hexademical character reference start state
    //TODO 12.2.5.77 Decimal character reference start state
    //TODO 12.2.5.78 Hexademical character reference state
    //TODO 12.2.5.79 Decimal character reference state
    //TODO 12.2.5.80 Numeric character reference end state

    // When a state says to flush code points consumed as a character reference,
    // it means that for each code point in the temporary buffer
    // (in the order they were added to the buffer) user agent must append the
    // code point from the buffer to the current attribute's value if the
    // character reference was consumed as part of an attribute, or emit
    // the code point as a character token otherwise.
    private void flushCodePoints() {
        switch (returnState) {
            case DATA:
                tokens.add(new CharacterToken(temporaryBuffer));
                break;
            case ATTRIBUTE_VALUE_DOUBLE_QUOTED:
            case ATTRIBUTE_VALUE_SINGLE_QUOTED:
            case ATTRIBUTE_VALUE_UNQUOTED:
                attribute.setValue(attribute.getValue().concat(temporaryBuffer.toString()));
                break;
            default:
                throw new RuntimeException("Unknown return state");
        }
        temporaryBuffer = null;
    }
}
