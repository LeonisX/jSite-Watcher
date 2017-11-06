package md.leonis.parser;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import md.leonis.parser.domain.Attribute;
import md.leonis.parser.domain.PageEncoding;
import md.leonis.parser.domain.State;
import md.leonis.parser.domain.token.CharacterToken;
import md.leonis.parser.domain.token.CommentToken;
import md.leonis.parser.domain.token.DoctypeToken;
import md.leonis.parser.domain.token.EndOfFileToken;
import md.leonis.parser.domain.token.EndTagToken;
import md.leonis.parser.domain.token.StartTagToken;
import md.leonis.parser.domain.token.TagToken;
import md.leonis.parser.domain.token.Token;

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
    private StartTagToken startTagToken;
    private EndTagToken endTagToken;
    private Attribute attribute;
    private CommentToken commentToken;
    private DoctypeToken doctypeToken;
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

        //TODO make as EnumMap
        while (position <= length) {
            switch (state) {
                case DATA:
                    inData();
                    break;
                case RCDATA:
                    inRcData();
                    break;
                case RAWTEXT:
                    inRawText();
                    break;
                case SCRIPT_DATA:
                    inScriptData();
                    break;
                case PLAINTEXT:
                    inPlainText();
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
                case RCDATA_LESS_THAN_SIGN:
                    inRcDataLessThanSign();
                    break;
                case RCDATA_END_TAG_OPEN:
                    inRcDataEndTagOpen();
                    break;
                case RCDATA_END_TAG_NAME:
                    inRcDataEndTagName();
                    break;
                case RAWTEXT_LESS_THAN_SIGN:
                    inRawTextLessThanSign();
                    break;
                case RAWTEXT_END_TAG_OPEN:
                    inRawTextEndTagOpen();
                    break;
                case RAWTEXT_END_TAG_NAME:
                    inRawTextEndTagName();
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
                case BOGUS_COMMENT:
                    inBogusComment();
                    break;
                case MARKUP_DECLARATION_OPEN:
                    inMarkupDeclarationOpen();
                    break;
                case COMMENT_START:
                    inCommentStart();
                    break;
                case COMMENT_START_DASH:
                    inCommentStartDash();
                    break;
                case COMMENT:
                    inComment();
                    break;
                case COMMENT_LESS_THAN_SIGN:
                    inCommentLessThanSign();
                    break;
                case COMMENT_LESS_THAN_SIGN_BANG:
                    inCommentLessThanSignBang();
                    break;
                case COMMENT_LESS_THAN_SIGN_BANG_DASH:
                    inCommentLessThanSignBangDash();
                    break;
                case COMMENT_LESS_THAN_SIGN_BANG_DASH_DASH:
                    inCommentLessThanSignBangDashDash();
                    break;
                case COMMENT_END_DASH:
                    inCommentEndDash();
                    break;
                case COMMENT_END:
                    inCommentEnd();
                    break;
                case COMMENT_END_BANG:
                    inCommentEndBang();
                    break;
                case DOCTYPE:
                    inDoctype();
                    break;
                case BEFORE_DOCTYPE_NAME:
                    inBeforeDoctypeName();
                    break;
                case DOCTYPE_NAME:
                    inDoctypeName();
                    break;
                case AFTER_DOCTYPE_NAME:
                    inAfterDoctypeName();
                    break;
                case AFTER_DOCTYPE_PUBLIC_KEYWORD:
                    inAfterDoctypePublicKeyword();
                    break;
                case BEFORE_DOCTYPE_PUBLIC_IDENTIFIER:
                    inBeforeDoctypePublicIdentifier();
                    break;
                case DOCTYPE_PUBLIC_IDENTIFIER_DOUBLE_QUOTED:
                    inDoctypePublicIdentifierDoubleQuoted();
                    break;
                case DOCTYPE_PUBLIC_IDENTIFIER_SINGLE_QUOTED:
                    inDoctypePublicIdentifierSingleQuoted();
                    break;
                case AFTER_DOCTYPE_PUBLIC_IDENTIFIER:
                    inAfterDoctypePublicIdentifier();
                    break;
                case BETWEEN_DOCTYPE_PUBLIC_AND_SYSTEM_IDENTIFIERS:
                    inBetweenDoctypePublicAndSystemIdentifiers();
                    break;
                case AFTER_DOCTYPE_SYSTEM_KEYWORD:
                    inAfterDoctypeSystemKeyword();
                    break;
                case BEFORE_DOCTYPE_SYSTEM_IDENTIFIER:
                    inBeforeDoctypeSystemIdentifier();
                    break;
                case DOCTYPE_SYSTEM_IDENTIFIER_DOUBLE_QUOTED:
                    inDoctypeSystemIdentifierDoubleQuoted();
                    break;
                case DOCTYPE_SYSTEM_IDENTIFIER_SINGLE_QUOTED:
                    inDoctypeSystemIdentifierSingleQuoted();
                    break;
                case AFTER_DOCTYPE_SYSTEM_IDENTIFIER:
                    inAfterDoctypeSystemIdentifier();
                    break;
                case BOGUS_DOCTYPE:
                    inBogusDoctype();
                    break;
                case CDATA_SECTION:
                    inCDataSection();
                    break;
                case CDATA_SECTION_BRACKET:
                    inCDataSectionBracket();
                    break;
                case CDATA_SECTION_END:
                    inCDataSectionEnd();
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
        debug("inData");
        // Consume the next input character:
        // EOF: Emit an end-of-file token.
        if (position == length) {
            tokens.add(new EndOfFileToken());
            position++;
            return;
        }
        char c = htmlString.charAt(position);
        switch (c) {
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
                //TODO optionally after refactor (also in all other places)
                tokens.add(new CharacterToken((char) 0xFFFD));
                //TODO pass to special class
                //TODO where optionally process exceptions and show detailed logs
                log.error("unexpected-null-character");
                position++;
                break;
            default:
                // Anything else: Emit the current input character as a character token.
                tokens.add(new CharacterToken(c));
                position++;
        }
    }

    //12.2.5.2 RCDATA state
    private void inRcData() {
        debug("inRcData");
        // Consume the next input character:
        // EOF: Emit an end-of-file token.
        if (position == length) {
            tokens.add(new EndOfFileToken());
            position++;
            return;
        }
        char c = htmlString.charAt(position);
        switch (c) {
            // U+0026 AMPERSAND (&)
            case '&':
                // Set the return state to the RCDATA state. Switch to the character reference state.
                returnState = State.RCDATA;
                state = State.CHARACTER_REFERENCE;
                position++;
                break;
            // U+003C LESS-THAN SIGN (<)
            case '<':
                // Switch to the RCDATA less-than sign state.
                state = State.RCDATA_LESS_THAN_SIGN;
                position++;
                break;
            // U+0000 NULL
            case 0x0000:
                // U+0000 NULL: This is an unexpected-null-character parse error.
                // Emit the current input character as a character token.
                tokens.add(new CharacterToken((char) 0xFFFD));
                log.error("unexpected-null-character");
                position++;
                break;
            default:
                // Anything else: Emit the current input character as a character token.
                tokens.add(new CharacterToken(c));
                position++;
        }
    }

    //12.2.5.3 RAWTEXT state
    private void inRawText() {
        debug("inRawText");
        // Consume the next input character:
        // EOF: Emit an end-of-file token.
        if (position == length) {
            tokens.add(new EndOfFileToken());
            position++;
            return;
        }
        char c = htmlString.charAt(position);
        switch (c) {
            // U+003C LESS-THAN SIGN (<)
            case '<':
                // Switch to the RAWTEXT less-than sign state.
                state = State.RAWTEXT_LESS_THAN_SIGN;
                position++;
                break;
            // U+0000 NULL
            case 0x0000:
                // U+0000 NULL: This is an unexpected-null-character parse error.
                // Emit the current input character as a character token.
                tokens.add(new CharacterToken((char) 0xFFFD));
                log.error("unexpected-null-character");
                position++;
                break;
            default:
                // Anything else: Emit the current input character as a character token.
                tokens.add(new CharacterToken(c));
                position++;
        }
    }

    //12.2.5.4 Script data state
    private void inScriptData() {
        debug("inScriptData");
        // Consume the next input character:
        // EOF: Emit an end-of-file token.
        if (position == length) {
            tokens.add(new EndOfFileToken());
            position++;
            return;
        }
        char c = htmlString.charAt(position);
        switch (c) {
            // U+003C LESS-THAN SIGN (<)
            case '<':
                // Switch to the script data less-than sign state.
                state = State.SCRIPT_LESS_THAN_SIGN;
                position++;
                break;
            // U+0000 NULL
            case 0x0000:
                // U+0000 NULL: This is an unexpected-null-character parse error.
                // Emit the current input character as a character token.
                tokens.add(new CharacterToken((char) 0xFFFD));
                log.error("unexpected-null-character");
                position++;
                break;
            default:
                // Anything else: Emit the current input character as a character token.
                tokens.add(new CharacterToken(c));
                position++;
        }
    }

    //12.2.5.5 PLAINTEXT state
    private void inPlainText() {
        debug("inPlainText");
        // Consume the next input character:
        // EOF: Emit an end-of-file token.
        if (position == length) {
            tokens.add(new EndOfFileToken());
            position++;
            return;
        }
        char c = htmlString.charAt(position);
        switch (c) {
            // U+0000 NULL
            case 0x0000:
                // U+0000 NULL: This is an unexpected-null-character parse error.
                // Emit the current input character as a character token.
                tokens.add(new CharacterToken((char) 0xFFFD));
                log.error("unexpected-null-character");
                position++;
                break;
            default:
                // Anything else: Emit the current input character as a character token.
                tokens.add(new CharacterToken(c));
                position++;
        }
    }

    //12.2.5.6 Tag open state
    private void inTagOpen() {
        debug("inTagOpen");
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
                    startTagToken = new StartTagToken("");
                    tagToken = startTagToken;
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
        debug("inEndTagOpen");
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
                    endTagToken = new EndTagToken("");
                    tagToken = endTagToken;
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
        debug("inTagName");
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

    //12.2.5.9 RCDATA less-than sign state
    private void inRcDataLessThanSign() {
        debug("inRcDataLessThanSign");
        // Consume the next input character:
        char c = htmlString.charAt(position);
        switch (c) {
            // U+002F SOLIDUS (/)
            case '/':
                //Set the temporary buffer to the empty string. Switch to the RCDATA end tag open state.
                temporaryBuffer = new StringBuilder("");
                state = State.RCDATA_END_TAG_OPEN;
                position++;
                break;
            default:
                // Anything else: Emit a U+003C LESS-THAN SIGN character token.
                tokens.add(new CharacterToken((char) 0x003C));
                // Reconsume in the RCDATA state.
                state = State.RCDATA;
        }
    }

    //12.2.5.10 RCDATA end tag open state
    private void inRcDataEndTagOpen() {
        debug("inRcDataEndTagOpen");
        // Consume the next input character:
        char c = htmlString.charAt(position);
        // ASCII alpha
        if (Constants.ASCII_ALPHA.contains(c)) {
            // Create a new end tag token, set its tag name to the empty string.
            endTagToken = new EndTagToken("");
            tagToken = endTagToken;
            // Reconsume in the RCDATA end tag name state.
            state = State.RCDATA_END_TAG_NAME;
        } else {
            // Emit a U+003C LESS-THAN SIGN character token and a U+002F SOLIDUS character token
            tokens.add(new CharacterToken((char) 0x003C));
            tokens.add(new CharacterToken((char) 0x002F));
            // Reconsume in the RCDATA state.
            state = State.RCDATA;
        }
    }

    //12.2.5.11 RCDATA end tag name state
    private void inRcDataEndTagName() {
        debug("inRcDataEndTagName");
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
                // If the current end tag token is an appropriate end tag token,
                if (endTagToken.getName().equals(startTagToken.getName())) {
                    // then switch to the before attribute name state.
                    state = State.BEFORE_ATTRIBUTE_NAME;
                    position++;
                } else {
                    // Otherwise, treat it as per the "anything else" entry below.
                    rcDataAnythingElse();
                }
                break;
            // U+002F SOLIDUS (/)
            case '/':
                //If the current end tag token is an appropriate end tag token,
                if (endTagToken.getName().equals(startTagToken.getName())) {
                    // then switch to the self-closing start tag state.
                    state = State.SELF_CLOSING_START_TAG;
                    position++;
                } else {
                    // Otherwise, treat it as per the "anything else" entry below.
                    rcDataAnythingElse();
                }
                break;
            // U+003E GREATER-THAN SIGN (>)
            case '>':
                //If the current end tag token is an appropriate end tag token,
                if (endTagToken.getName().equals(startTagToken.getName())) {
                    // then switch to the data state and emit the current tag token
                    state = State.DATA;
                    tokens.add(tagToken);
                    position++;
                } else {
                    // Otherwise, treat it as per the "anything else" entry below.
                    rcDataAnythingElse();
                }
                break;
            default:
                // ASCII upper alpha
                if (Constants.ASCII_UPPER_ALPHA.contains(c)) {
                    // Append the lowercase version of the current input character
                    // (add 0x0020 to the character's code point) to the current tag token's tag name.
                    tagToken.setName(tagToken.getName() + Character.toLowerCase(c));
                    // Append the current input character to the temporary buffer.
                    temporaryBuffer.append(c);
                    position++;
                } else
                    // ASCII lower alpha
                    if (Constants.ASCII_LOWER_ALPHA.contains(c)) {
                        // Append the current input character to the current tag token's tag name.
                        tagToken.setName(tagToken.getName() + c);
                        // Append the current input character to the temporary buffer.
                        temporaryBuffer.append(c);
                        position++;
                    }
                    // Anything else
                    else {
                        rcDataAnythingElse();
                    }
        }
    }

    private void rcDataAnythingElse() {
        // Emit a U+003C LESS-THAN SIGN character token, a U+002F SOLIDUS character token,
        // and a character token for each of the characters in the temporary buffer
        // (in the order they were added to the buffer).
        tokens.add(new CharacterToken((char) 0x003C));
        tokens.add(new CharacterToken((char) 0x002F));
        for (char ch: temporaryBuffer.toString().toCharArray()) {
            tokens.add(new CharacterToken(ch));
        }
        // Reconsume in the RCDATA state.
        state = State.RCDATA;
    }

    //12.2.5.12 RAWTEXT less-than sign state
    private void inRawTextLessThanSign() {
        debug("inRawTextLessThanSign");
        // Consume the next input character:
        char c = htmlString.charAt(position);
        switch (c) {
            // U+002F SOLIDUS (/)
            case '/':
                //Set the temporary buffer to the empty string. Switch to the RAWTEXT end tag open state.
                temporaryBuffer = new StringBuilder("");
                state = State.RAWTEXT_END_TAG_OPEN;
                position++;
                break;
            default:
                // Anything else: Emit a U+003C LESS-THAN SIGN character token.
                tokens.add(new CharacterToken((char) 0x003C));
                // Reconsume in the RAWTEXT state.
                state = State.RAWTEXT;
        }
    }

    //12.2.5.13 RAWTEXT end tag open state
    private void inRawTextEndTagOpen() {
        debug("inRawTextEndTagOpen");
        // Consume the next input character:
        char c = htmlString.charAt(position);
        // ASCII alpha
        if (Constants.ASCII_ALPHA.contains(c)) {
            // Create a new end tag token, set its tag name to the empty string.
            endTagToken = new EndTagToken("");
            tagToken = endTagToken;
            // Reconsume in the RAWTEXT end tag name state.
            state = State.RAWTEXT_END_TAG_NAME;
        } else {
            // Emit a U+003C LESS-THAN SIGN character token and a U+002F SOLIDUS character token
            tokens.add(new CharacterToken((char) 0x003C));
            tokens.add(new CharacterToken((char) 0x002F));
            //  Reconsume in the RAWTEXT state.
            state = State.RAWTEXT;
        }
    }

    //12.2.5.14 RAWTEXT end tag name state
    private void inRawTextEndTagName() {
        debug("inRawTextEndTagName");
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
                // If the current end tag token is an appropriate end tag token,
                if (endTagToken.getName().equals(startTagToken.getName())) {
                    // then switch to the before attribute name state.
                    state = State.BEFORE_ATTRIBUTE_NAME;
                    position++;
                } else {
                    // Otherwise, treat it as per the "anything else" entry below.
                    rawTextAnythingElse();
                }
                break;
            // U+002F SOLIDUS (/)
            case '/':
                //If the current end tag token is an appropriate end tag token,
                if (endTagToken.getName().equals(startTagToken.getName())) {
                    // then switch to the self-closing start tag state.
                    state = State.SELF_CLOSING_START_TAG;
                    position++;
                } else {
                    // Otherwise, treat it as per the "anything else" entry below.
                    rawTextAnythingElse();
                }
                break;
            // U+003E GREATER-THAN SIGN (>)
            case '>':
                //If the current end tag token is an appropriate end tag token,
                if (endTagToken.getName().equals(startTagToken.getName())) {
                    // then switch to the data state and emit the current tag token
                    state = State.DATA;
                    tokens.add(tagToken);
                    position++;
                } else {
                    // Otherwise, treat it as per the "anything else" entry below.
                    rawTextAnythingElse();
                }
                break;
            default:
                // ASCII upper alpha
                if (Constants.ASCII_UPPER_ALPHA.contains(c)) {
                    // Append the lowercase version of the current input character
                    // (add 0x0020 to the character's code point) to the current tag token's tag name.
                    tagToken.setName(tagToken.getName() + Character.toLowerCase(c));
                    // Append the current input character to the temporary buffer.
                    temporaryBuffer.append(c);
                    position++;
                } else
                    // ASCII lower alpha
                    if (Constants.ASCII_LOWER_ALPHA.contains(c)) {
                        // Append the current input character to the current tag token's tag name.
                        tagToken.setName(tagToken.getName() + c);
                        // Append the current input character to the temporary buffer.
                        temporaryBuffer.append(c);
                        position++;
                    }
                    // Anything else
                    else {
                        rawTextAnythingElse();
                    }
        }
    }

    private void rawTextAnythingElse() {
        // Emit a U+003C LESS-THAN SIGN character token, a U+002F SOLIDUS character token,
        // and a character token for each of the characters in the temporary buffer
        // (in the order they were added to the buffer).
        tokens.add(new CharacterToken((char) 0x003C));
        tokens.add(new CharacterToken((char) 0x002F));
        for (char ch: temporaryBuffer.toString().toCharArray()) {
            tokens.add(new CharacterToken(ch));
        }
        // Reconsume in the RAWTEXT state.
        state = State.RAWTEXT;
    }

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
        debug("inBeforeAttributeName");
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
        debug("inAttributeName");
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
        debug("inAfterAttributeName");
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
        debug("inBeforeAttributeValue");
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
        debug("inAttributeValueDoubleQuoted");
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
        debug("inAttributeValueSingleQuoted");
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
        debug("inAttributeValueUnquoted");
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
        debug("inAfterAttributeValue");
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
        debug("inSelfClosingStartTag");
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

    //12.2.5.41 Bogus comment state
    private void inBogusComment() {
        debug("inBogusComment");
        // Consume the next input character:
        // EOF: Emit the comment. Emit an end-of-file token.
        if (position == length) {
            tokens.add(commentToken);
            tokens.add(new EndOfFileToken());
            position++;
            return;
        }
        char c = htmlString.charAt(position);
        switch (c) {
            //U+003E GREATER-THAN SIGN (>)
            case '>':
                // Switch to the data state.
                state = State.DATA;
                // Emit the comment token.
                tokens.add(commentToken);
                position++;
                break;
            // U+0000 NULL
            case 0x0000:
                // This is an unexpected-null-character parse error.
                log.error("unexpected-null-character");
                // Append a U+FFFD REPLACEMENT CHARACTER character to the comment token's data.
                commentToken.setData(commentToken.getData() + (char) 0xFFFD);
                position++;
                break;
            default:
                // Anything else: Append the current input character to the comment token's data.
                commentToken.setData(commentToken.getData() + c);
                break;
        }
    }

    //12.2.5.42 Markup declaration open state
    private void inMarkupDeclarationOpen() {
        debug("inMarkupDeclarationOpen");
        // If the next few characters are:
        // Two U+002D HYPHEN-MINUS characters (-)
        // Consume those two characters, create a comment token whose data is the empty string,
        // and switch to the comment start state.
        String string = substring(2);
        if (string.equals("--")) {
            position += 2;
            commentToken = new CommentToken("");
            state = State.COMMENT_START;
            return;
        }
        // ASCII case-insensitive match for the word "DOCTYPE"
        // Consume those characters and switch to the DOCTYPE state.
        string = substring(7).toUpperCase();
        if (string.equals("DOCTYPE")) {
            position += 7;
            state = State.DOCTYPE;
            return;
        }
        // Case-sensitive match for the string "[CDATA[" (the five uppercase letters "CDATA" with a
        // U+005B LEFT SQUARE BRACKET character before and after)
        string = substring(7).toUpperCase();
        if (string.equals("[CDATA[")) {
            // Consume those characters.
            position += 7;
            // If there is an adjusted current node and it is not an element in the HTML namespace,
            // then switch to the CDATA section state.
            // TODO for now we parse only pure HTML, so, threat as comment
            // Otherwise, this is a cdata-in-html-content parse error.
            log.error("cdata-in-html-content");
            // Create a comment token whose data is the "[CDATA[" string.
            commentToken = new CommentToken("[CDATA[");
            // Switch to the bogus comment state.
            state = State.BOGUS_COMMENT;
            return;
        }
        //Anything else: This is an incorrectly-opened-comment parse error.
        log.error("incorrectly-opened-comment");
        // Create a comment token whose data is the empty string.
        commentToken = new CommentToken("");
        // Switch to the bogus comment state (don't consume anything in the current state).
        state = State.BOGUS_COMMENT;
    }

    //12.2.5.43 Comment start state
    private void inCommentStart() {
        debug("inCommentStart");
        // Consume the next input character:
        char c = htmlString.charAt(position);
        switch (c) {
            // U+002D HYPHEN-MINUS (-)
            case '-':
                // Switch to the comment start dash state.
                state = State.COMMENT_START_DASH;
                position++;
                break;
            // U+003E GREATER-THAN SIGN (>)
            case '>':
                // This is an abrupt-closing-of-empty-comment parse error.
                log.error("abrupt-closing-of-empty-comment");
                // Switch to the data state. Emit the comment token.
                state = State.DATA;
                tokens.add(commentToken);
                position++;
                break;
            default:
                // Reconsume in the comment state.
                state = State.COMMENT;
                break;
        }
    }

    //12.2.5.44 Comment start dash state
    private void inCommentStartDash() {
        debug("inCommentStartDash");
        // Consume the next input character:
        // EOF:
        if (position == length) {
            // This is an eof-in-comment parse error.
            log.error("eof-in-comment");
            // Emit the comment token. Emit an end-of-file token.
            tokens.add(commentToken);
            tokens.add(new EndOfFileToken());
            position++;
            return;
        }
        char c = htmlString.charAt(position);
        switch (c) {
            // U+002D HYPHEN-MINUS (-)
            case '-':
                // Switch to the comment end state
                state = State.COMMENT_END;
                position++;
                break;
            // U+003E GREATER-THAN SIGN (>)
            case '>':
                // This is an abrupt-closing-of-empty-comment parse error.
                log.error("abrupt-closing-of-empty-comment");
                // Switch to the data state. Emit the comment token.
                state = State.DATA;
                tokens.add(commentToken);
                position++;
                break;
            default:
                // Append a U+002D HYPHEN-MINUS character (-) to the comment token's data.
                // Reconsume in the comment state.
                commentToken.setData(commentToken.getData() + '-');
                state = State.COMMENT;
                break;
        }
    }

    //12.2.5.45 Comment state
    private void inComment() {
        debug("inComment");
        // Consume the next input character:
        // EOF:
        if (position == length) {
            // This is an eof-in-comment parse error.
            log.error("eof-in-comment");
            // Emit the comment token. Emit an end-of-file token.
            tokens.add(commentToken);
            tokens.add(new EndOfFileToken());
            position++;
            return;
        }
        char c = htmlString.charAt(position);
        switch (c) {
            // U+003C LESS-THAN SIGN (<)
            case '<':
                // Append the current input character to the comment token's data.
                // Switch to the comment less-than sign state.
                commentToken.setData(commentToken.getData() + c);
                state = State.COMMENT_LESS_THAN_SIGN;
                position++;
                break;
            // U+002D HYPHEN-MINUS (-)
            case '-':
                // Switch to the comment end dash state.
                state = State.COMMENT_END_DASH;
                position++;
                break;
            // U+0000 NULL
            case 0x0000:
                // This is an unexpected-null-character parse error.
                log.error("unexpected-null-character");
                // Append a U+FFFD REPLACEMENT CHARACTER character to the comment token's data.
                commentToken.setData(commentToken.getData() + (char) 0xFFFD);
                position++;
                return;
            default:
                // Append the current input character to the comment token's data.
                commentToken.setData(commentToken.getData() + c);
                position++;
                break;
        }
    }

    //12.2.5.46 Comment less-than sign state
    private void inCommentLessThanSign() {
        debug("inCommentLessThanSign");
        // Consume the next input character:
        char c = htmlString.charAt(position);
        switch (c) {
            // U+0021 EXCLAMATION MARK (!)
            case '!':
                // Append the current input character to the comment token's data.
                commentToken.setData(commentToken.getData() + c);
                // Switch to the comment less-than sign bang state.
                state = State.COMMENT_LESS_THAN_SIGN_BANG;
                position++;
                break;
            // U+003C LESS-THAN SIGN (<)
            case '<':
                // Append the current input character to the comment token's data.
                commentToken.setData(commentToken.getData() + c);
                position++;
                break;
            default:
                // Reconsume in the comment state.
                state = State.COMMENT;
                break;
        }
    }

    //12.2.5.47 Comment less-than sign bang state
    private void inCommentLessThanSignBang() {
        debug("inCommentLessThanSignBang");
        // Consume the next input character:
        char c = htmlString.charAt(position);
        switch (c) {
            // U+002D HYPHEN-MINUS (-)
            case '-':
                // Switch to the comment less-than sign bang dash state.
                state = State.COMMENT_LESS_THAN_SIGN_BANG_DASH;
                position++;
                break;
            default:
                // Reconsume in the comment state.
                state = State.COMMENT;
                break;
        }
    }

    //12.2.5.48 Comment less-than sign bang dash state
    private void inCommentLessThanSignBangDash() {
        debug("inCommentLessThanSignBangDash");
        // Consume the next input character:
        char c = htmlString.charAt(position);
        switch (c) {
            // U+002D HYPHEN-MINUS (-)
            case '-':
                // Switch to the comment less-than sign bang dash dash state.
                state = State.COMMENT_LESS_THAN_SIGN_BANG_DASH_DASH;
                position++;
                break;
            default:
                // Reconsume in the comment end dash state.
                state = State.COMMENT_END_DASH;
                break;
        }
    }

    //12.2.5.49 Comment less-than sign bang dash dash state
    private void inCommentLessThanSignBangDashDash() {
        debug("inCommentLessThanSignBangDashDash");
        // Consume the next input character:
        // EOF:
        if (position == length) {
            // Reconsume in the comment end state.
            state = State.COMMENT_END;
            return;
        }
        char c = htmlString.charAt(position);
        switch (c) {
            // U+003E GREATER-THAN SIGN (>)
            case '>':
                // Reconsume in the comment end state.
                state = State.COMMENT_END;
                break;
            default:
                // This is a nested-comment parse error. Reconsume in the comment end state.
                log.error("nested-comment");
                state = State.COMMENT_END;
                break;
        }
    }

    //12.2.5.50 Comment end dash state
    private void inCommentEndDash() {
        debug("inCommentEndDash");
        // Consume the next input character:
        // EOF:
        if (position == length) {
            // This is an eof-in-comment parse error.
            log.error("eof-in-comment");
            // Emit the comment token. Emit an end-of-file token.
            tokens.add(commentToken);
            tokens.add(new EndOfFileToken());
            return;
        }
        char c = htmlString.charAt(position);
        switch (c) {
            // U+002D HYPHEN-MINUS (-)
            case '-':
                // Switch to the comment end state
                state = State.COMMENT_END;
                position++;
                break;
            default:
                // Append a U+002D HYPHEN-MINUS character (-) to the comment token's data.
                commentToken.setData(commentToken.getData() + '-');
                // Reconsume in the comment state.
                state = State.COMMENT;
                break;
        }
    }

    //12.2.5.51 Comment end state
    private void inCommentEnd() {
        debug("inCommentEnd");
        // Consume the next input character:
        // EOF:
        if (position == length) {
            // This is an eof-in-comment parse error.
            log.error("eof-in-comment");
            // Emit the comment token. Emit an end-of-file token.
            tokens.add(commentToken);
            tokens.add(new EndOfFileToken());
            return;
        }
        char c = htmlString.charAt(position);
        switch (c) {
            // U+003E GREATER-THAN SIGN (>)
            case '>':
                // Switch to the data state. Emit the comment token.
                state = State.DATA;
                tokens.add(commentToken);
                position++;
                break;
            // U+0021 EXCLAMATION MARK (!)
            case '!':
                // Switch to the comment end bang state.
                state = State.COMMENT_END_BANG;
                position++;
                break;
            // U+002D HYPHEN-MINUS (-)
            case '-':
                // Append a U+002D HYPHEN-MINUS character (-) to the comment token's data.
                commentToken.setData(commentToken.getData() + '-');
                position++;
                break;
            default:
                // Append two U+002D HYPHEN-MINUS characters (-) to the comment token's data.
                commentToken.setData(commentToken.getData() + "--");
                // Reconsume in the comment state.
                state = State.COMMENT;
                break;
        }
    }

    //12.2.5.52 Comment end bang state
    private void inCommentEndBang() {
        debug("inCommentEndBang");
        // Consume the next input character:
        // EOF:
        if (position == length) {
            // This is an eof-in-comment parse error.
            log.error("eof-in-comment");
            // Emit the comment token. Emit an end-of-file token.
            tokens.add(commentToken);
            tokens.add(new EndOfFileToken());
            return;
        }
        char c = htmlString.charAt(position);
        switch (c) {
            // U+002D HYPHEN-MINUS (-)
            case '-':
                // Append two U+002D HYPHEN-MINUS characters (-)
                // and a U+0021 EXCLAMATION MARK character (!) to the comment token's data.
                commentToken.setData(commentToken.getData() + "--!");
                // Switch to the comment end dash state.
                state = State.COMMENT_END_DASH;
                position++;
                break;
            // U+003E GREATER-THAN SIGN (>)
            case '>':
                // This is an incorrectly-closed-comment parse error.
                log.error("incorrectly-closed-comment");
                // Switch to the data state. Emit the comment token.
                state = State.DATA;
                tokens.add(commentToken);
                position++;
                break;
            default:
                // Append two U+002D HYPHEN-MINUS characters (-)
                // and a U+0021 EXCLAMATION MARK character (!) to the comment token's data.
                commentToken.setData(commentToken.getData() + "--!");
                // Reconsume in the comment state.
                state = State.COMMENT;
                break;
        }
    }

    //12.2.5.53 DOCTYPE state
    private void inDoctype() {
        debug("inDoctype");
        // Consume the next input character:
        // EOF:
        if (position == length) {
            // EOF: This is an eof-in-doctype parse error.
            log.error("eof-in-doctype");
            // Create a new DOCTYPE token.
            // Set its force-quirks flag to on. Emit the token. Emit an end-of-file token.
            doctypeToken = new DoctypeToken();
            doctypeToken.setForceQuirks(true);
            tokens.add(doctypeToken);
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
                // Switch to the before DOCTYPE name state.
                state = State.BEFORE_DOCTYPE_NAME;
                position++;
                break;
            // U+003E GREATER-THAN SIGN (>)
            case '>':
                // Reconsume in the before DOCTYPE name state.
                state = State.BEFORE_DOCTYPE_NAME;
                break;
            default:
                // Anything else: This is a missing-whitespace-before-doctype-name parse error.
                log.error("missing-whitespace-before-doctype-name");
                // Reconsume in the before DOCTYPE name state.
                state = State.BEFORE_DOCTYPE_NAME;
                break;
        }
    }

    //12.2.5.54 Before DOCTYPE name state
    private void inBeforeDoctypeName() {
        debug("inBeforeDoctypeName");
        // Consume the next input character:
        // EOF:
        if (position == length) {
            // EOF: This is an eof-in-doctype parse error.
            log.error("eof-in-doctype");
            // Create a new DOCTYPE token.
            // Set its force-quirks flag to on. Emit the token. Emit an end-of-file token.
            doctypeToken = new DoctypeToken();
            doctypeToken.setForceQuirks(true);
            tokens.add(doctypeToken);
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
            // U+0000 NULL
            case 0x0000:
                // This is an unexpected-null-character parse error.
                log.error("unexpected-null-character");
                // Create a new DOCTYPE token.
                doctypeToken = new DoctypeToken();
                // Set the token's name to a U+FFFD REPLACEMENT CHARACTER character.
                //TODO optionally
                doctypeToken.setName("" + (char) 0xFFFD);
                // Switch to the DOCTYPE name state.
                state = State.DOCTYPE_NAME;
                position++;
                return;
            // U+003E GREATER-THAN SIGN (>)
            case '>':
                // This is a missing-doctype-name parse error.
                log.error("missing-doctype-name");
                // Create a new DOCTYPE token.
                doctypeToken = new DoctypeToken();
                // Set its force-quirks flag to on. Emit the token.
                doctypeToken.setForceQuirks(true);
                tokens.add(doctypeToken);
                // Switch to the data state.
                state = State.DATA;
                position++;
                return;
            default:
                // ASCII upper alpha
                if (Constants.ASCII_LOWER_ALPHA.contains(c)) {
                    // Create a new DOCTYPE token.
                    doctypeToken = new DoctypeToken();
                    // Set the token's name to the lowercase version of the current input character
                    // (add 0x0020 to the character's code point).
                    //TODO preserve original case too
                    doctypeToken.setName("" + Character.toLowerCase(c));
                    // Switch to the DOCTYPE name state.
                    state = State.DOCTYPE_NAME;
                    position++;
                } else {
                    // Anything else: Create a new DOCTYPE token.
                    doctypeToken = new DoctypeToken();
                    // Set the token's name to the current input character. Switch to the DOCTYPE name state.
                    doctypeToken.setName("" + c);
                    state = State.DOCTYPE_NAME;
                    position++;
                }
                break;
        }
    }

    //12.2.5.55 DOCTYPE name state
    private void inDoctypeName() {
        debug("inDoctypeName");
        // Consume the next input character:
        // EOF:
        if (position == length) {
            // EOF: This is an eof-in-doctype parse error.
            log.error("eof-in-doctype");
            // Set its force-quirks flag to on. Emit the token. Emit an end-of-file token.
            doctypeToken.setForceQuirks(true);
            tokens.add(doctypeToken);
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
                // Switch to the after DOCTYPE name state.
                state = State.AFTER_DOCTYPE_NAME;
                position++;
                break;
            // U+0000 NULL
            case 0x0000:
                // This is an unexpected-null-character parse error.
                log.error("unexpected-null-character");
                // Append a U+FFFD REPLACEMENT CHARACTER character to the current DOCTYPE token's name.
                //TODO optionally
                doctypeToken.setName(doctypeToken.getName() + (char) 0xFFFD);
                position++;
                return;
            // U+003E GREATER-THAN SIGN (>)
            case '>':
                // Switch to the data state.
                state = State.DATA;
                // Emit the current DOCTYPE token.
                tokens.add(doctypeToken);
                position++;
                return;
            default:
                // ASCII upper alpha
                if (Constants.ASCII_LOWER_ALPHA.contains(c)) {
                    // Append the lowercase version of the current input character
                    // (add 0x0020 to the character's code point) to the current DOCTYPE token's name.
                    //TODO preserve original case too
                    doctypeToken.setName(doctypeToken.getName() + Character.toLowerCase(c));
                    // Switch to the DOCTYPE name state.
                    state = State.DOCTYPE_NAME;
                    position++;
                } else {
                    // Anything else:
                    // Append the current input character to the current DOCTYPE token's name.
                    doctypeToken.setName(doctypeToken.getName() + c);
                    position++;
                }
                break;
        }
    }

    //12.2.5.56 After DOCTYPE name state
    private void inAfterDoctypeName() {
        debug("inAfterDoctypeName");
        // Consume the next input character:
        // EOF:
        if (position == length) {
            // EOF: This is an eof-in-doctype parse error.
            log.error("eof-in-doctype");
            // Set its force-quirks flag to on. Emit the token. Emit an end-of-file token.
            doctypeToken.setForceQuirks(true);
            tokens.add(doctypeToken);
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
            // U+003E GREATER-THAN SIGN (>)
            case '>':
                // Switch to the data state.
                state = State.DATA;
                // Emit the current DOCTYPE token.
                tokens.add(doctypeToken);
                position++;
                return;
            default:
                String chunk = substring(6);
                // If the six characters starting from the current input character are an ASCII case-insensitive
                // match for the word "PUBLIC",
                if ("PUBLIC".equals(chunk)) {
                    // then consume those characters and switch to the
                    // after DOCTYPE public keyword state.
                    position += 6;
                    state = State.AFTER_DOCTYPE_PUBLIC_KEYWORD;
                } else
                    // Otherwise, if the six characters starting from the current input character are an
                    // ASCII case-insensitive match for the word "SYSTEM",
                    if ("SYSTEM".equals(chunk)) {
                        // then consume those characters
                        // and switch to the after DOCTYPE system keyword state.
                        position += 6;
                        state = State.AFTER_DOCTYPE_SYSTEM_KEYWORD;
                    } else {
                        // Otherwise, this is an invalid-character-sequence-after-doctype-name parse error.
                        log.error("invalid-character-sequence-after-doctype-name");
                        // Set the DOCTYPE token's force-quirks flag to on.
                        doctypeToken.setForceQuirks(true);
                        // Reconsume in the bogus DOCTYPE state.
                        state = State.BOGUS_DOCTYPE;
                    }
                break;
        }
    }

    //12.2.5.57 After DOCTYPE public keyword state
    private void inAfterDoctypePublicKeyword() {
        debug("inAfterDoctypePublicKeyword");
        // Consume the next input character:
        // EOF:
        if (position == length) {
            // EOF: This is an eof-in-doctype parse error.
            log.error("eof-in-doctype");
            // Set the DOCTYPE token's force-quirks flag to on. Emit that DOCTYPE token. Emit an end-of-file token.
            doctypeToken.setForceQuirks(true);
            tokens.add(doctypeToken);
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
                // Switch to the before DOCTYPE public identifier state.
                state = State.BEFORE_DOCTYPE_PUBLIC_IDENTIFIER;
                position++;
                break;
            // U+0022 QUOTATION MARK (")
            case '"':
                // This is a missing-whitespace-after-doctype-public-keyword parse error.
                log.error("missing-whitespace-after-doctype-public-keyword");
                // Set the DOCTYPE token's public identifier to the empty string (not missing),
                doctypeToken.setPublicIdentifier("");
                // then switch to the DOCTYPE public identifier (double-quoted) state.
                state = State.DOCTYPE_PUBLIC_IDENTIFIER_DOUBLE_QUOTED;
                position++;
                return;
            // U+0027 APOSTROPHE (')
            case '\'':
                // This is a missing-whitespace-after-doctype-public-keyword parse error.
                log.error("missing-whitespace-after-doctype-public-keyword");
                // Set the DOCTYPE token's public identifier to the empty string (not missing),
                doctypeToken.setPublicIdentifier("");
                // then switch to the DOCTYPE public identifier (single-quoted) state.
                state = State.DOCTYPE_PUBLIC_IDENTIFIER_SINGLE_QUOTED;
                position++;
                return;
            // U+003E GREATER-THAN SIGN (>)
            case '>':
                // This is a missing-doctype-public-identifier parse error.
                log.error("missing-doctype-public-identifier");
                // Set the DOCTYPE token's force-quirks flag to on. Switch to the data state. Emit that DOCTYPE token.
                doctypeToken.setForceQuirks(true);
                state = State.DATA;
                // Emit the current DOCTYPE token.
                tokens.add(doctypeToken);
                position++;
                return;
            default:
                // Anything else: This is a missing-quote-before-doctype-public-identifier parse error.
                log.error("missing-quote-before-doctype-public-identifier");
                // Set the DOCTYPE token's force-quirks flag to on.
                doctypeToken.setForceQuirks(true);
                // Reconsume in the bogus DOCTYPE state.
                state = State.BOGUS_DOCTYPE;
                break;
        }
    }

    //12.2.5.58 Before DOCTYPE public identifier state
    private void inBeforeDoctypePublicIdentifier() {
        debug("inBeforeDoctypePublicIdentifier");
        // Consume the next input character:
        // EOF:
        if (position == length) {
            // EOF: This is an eof-in-doctype parse error.
            log.error("eof-in-doctype");
            // Set the DOCTYPE token's force-quirks flag to on. Emit that DOCTYPE token. Emit an end-of-file token.
            doctypeToken.setForceQuirks(true);
            tokens.add(doctypeToken);
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
            // U+0022 QUOTATION MARK (")
            case '"':
                // Set the DOCTYPE token's public identifier to the empty string (not missing),
                doctypeToken.setPublicIdentifier("");
                // then switch to the DOCTYPE public identifier (double-quoted) state.
                state = State.DOCTYPE_PUBLIC_IDENTIFIER_DOUBLE_QUOTED;
                position++;
                return;
            // U+0027 APOSTROPHE (')
            case '\'':
                // Set the DOCTYPE token's public identifier to the empty string (not missing),
                doctypeToken.setPublicIdentifier("");
                // then switch to the DOCTYPE public identifier (single-quoted) state.
                state = State.DOCTYPE_PUBLIC_IDENTIFIER_SINGLE_QUOTED;
                position++;
                return;
            // U+003E GREATER-THAN SIGN (>)
            case '>':
                // This is a missing-doctype-public-identifier parse error.
                log.error("missing-doctype-public-identifier");
                // Set the DOCTYPE token's force-quirks flag to on. Switch to the data state. Emit that DOCTYPE token.
                doctypeToken.setForceQuirks(true);
                state = State.DATA;
                // Emit the current DOCTYPE token.
                tokens.add(doctypeToken);
                position++;
                return;
            default:
                // Anything else: This is a missing-quote-before-doctype-public-identifier parse error.
                log.error("missing-quote-before-doctype-public-identifier");
                // Set the DOCTYPE token's force-quirks flag to on.
                doctypeToken.setForceQuirks(true);
                // Reconsume in the bogus DOCTYPE state.
                state = State.BOGUS_DOCTYPE;
                break;
        }
    }

    //12.2.5.59 DOCTYPE public identifier (double-quoted) state
    private void inDoctypePublicIdentifierDoubleQuoted() {
        debug("inDoctypePublicIdentifierDoubleQuoted");
        // Consume the next input character:
        // EOF:
        if (position == length) {
            // EOF: This is an eof-in-doctype parse error.
            log.error("eof-in-doctype");
            // Set the DOCTYPE token's force-quirks flag to on. Emit that DOCTYPE token. Emit an end-of-file token.
            doctypeToken.setForceQuirks(true);
            tokens.add(doctypeToken);
            tokens.add(new EndOfFileToken());
            position++;
            return;
        }
        char c = htmlString.charAt(position);
        switch (c) {
            // U+0022 QUOTATION MARK (")
            case '"':
                // Switch to the after DOCTYPE public identifier state.
                state = State.AFTER_DOCTYPE_PUBLIC_IDENTIFIER;
                position++;
                return;
            // U+0000 NULL
            case 0x0000:
                // This is an unexpected-null-character parse error.
                log.error("unexpected-null-character");
                // Append a U+FFFD REPLACEMENT CHARACTER character to the current DOCTYPE token's public identifier.
                doctypeToken.setPublicIdentifier(doctypeToken.getPublicIdentifier() + (char) 0xFFFD);
                position++;
                return;
            // U+003E GREATER-THAN SIGN (>)
            case '>':
                // This is an abrupt-doctype-public-identifier parse error.
                log.error("abrupt-doctype-public-identifier");
                // Set the DOCTYPE token's force-quirks flag to on. Switch to the data state. Emit that DOCTYPE token.
                doctypeToken.setForceQuirks(true);
                state = State.DATA;
                tokens.add(doctypeToken);
                position++;
                return;
            default:
                // Anything else: Append the current input character to the current DOCTYPE token's public identifier.
                doctypeToken.setPublicIdentifier(doctypeToken.getPublicIdentifier() + c);
                position++;
                break;
        }
    }

    //12.2.5.60 DOCTYPE public identifier (single-quoted) state
    private void inDoctypePublicIdentifierSingleQuoted() {
        debug("inDoctypePublicIdentifierSingleQuoted");
        // Consume the next input character:
        // EOF:
        if (position == length) {
            // EOF: This is an eof-in-doctype parse error.
            log.error("eof-in-doctype");
            // Set the DOCTYPE token's force-quirks flag to on. Emit that DOCTYPE token. Emit an end-of-file token.
            doctypeToken.setForceQuirks(true);
            tokens.add(doctypeToken);
            tokens.add(new EndOfFileToken());
            position++;
            return;
        }
        char c = htmlString.charAt(position);
        switch (c) {
            // U+0027 APOSTROPHE (')
            case '\'':
                // Switch to the after DOCTYPE public identifier state.
                state = State.AFTER_DOCTYPE_PUBLIC_IDENTIFIER;
                position++;
                return;
            // U+0000 NULL
            case 0x0000:
                // This is an unexpected-null-character parse error.
                log.error("unexpected-null-character");
                // Append a U+FFFD REPLACEMENT CHARACTER character to the current DOCTYPE token's public identifier.
                doctypeToken.setPublicIdentifier(doctypeToken.getPublicIdentifier() + (char) 0xFFFD);
                position++;
                return;
            // U+003E GREATER-THAN SIGN (>)
            case '>':
                // This is an abrupt-doctype-public-identifier parse error.
                log.error("abrupt-doctype-public-identifier");
                // Set the DOCTYPE token's force-quirks flag to on. Switch to the data state. Emit that DOCTYPE token.
                doctypeToken.setForceQuirks(true);
                state = State.DATA;
                tokens.add(doctypeToken);
                position++;
                return;
            default:
                // Anything else: Append the current input character to the current DOCTYPE token's public identifier.
                doctypeToken.setPublicIdentifier(doctypeToken.getPublicIdentifier() + c);
                position++;
                break;
        }
    }

    //12.2.5.61 After DOCTYPE public identifier state
    private void inAfterDoctypePublicIdentifier() {
        debug("inAfterDoctypePublicIdentifier");
        // Consume the next input character:
        // EOF:
        if (position == length) {
            // EOF: This is an eof-in-doctype parse error.
            log.error("eof-in-doctype");
            // Set the DOCTYPE token's force-quirks flag to on. Emit that DOCTYPE token. Emit an end-of-file token.
            doctypeToken.setForceQuirks(true);
            tokens.add(doctypeToken);
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
                // Switch to the between DOCTYPE public and system identifiers state.
                state = State.BETWEEN_DOCTYPE_PUBLIC_AND_SYSTEM_IDENTIFIERS;
                position++;
                break;
            // U+0022 QUOTATION MARK (")
            case '"':
                // This is a missing-whitespace-between-doctype-public-and-system-identifiers parse error.
                log.error("missing-whitespace-between-doctype-public-and-system-identifiers");
                // Set the DOCTYPE token's system identifier to the empty string (not missing),
                doctypeToken.setPublicIdentifier("");
                // then switch to the DOCTYPE system identifier (double-quoted) state.
                state = State.DOCTYPE_PUBLIC_IDENTIFIER_DOUBLE_QUOTED;
                position++;
                return;
            // U+0027 APOSTROPHE (')
            case '\'':
                // This is a missing-whitespace-between-doctype-public-and-system-identifiers parse error.
                log.error("missing-whitespace-between-doctype-public-and-system-identifiers");
                // Set the DOCTYPE token's public identifier to the empty string (not missing),
                doctypeToken.setPublicIdentifier("");
                // then switch to the DOCTYPE system identifier (single-quoted) state.
                state = State.DOCTYPE_PUBLIC_IDENTIFIER_SINGLE_QUOTED;
                position++;
                return;
            // U+003E GREATER-THAN SIGN (>)
            case '>':
                // Switch to the data state. Emit the current DOCTYPE token.
                state = State.DATA;
                tokens.add(doctypeToken);
                position++;
                return;
            default:
                // Anything else: This is a missing-quote-before-doctype-system-identifier parse error.
                log.error("missing-quote-before-doctype-system-identifier");
                // Set the DOCTYPE token's force-quirks flag to on.
                doctypeToken.setForceQuirks(true);
                // Reconsume in the bogus DOCTYPE state.
                state = State.BOGUS_DOCTYPE;
                break;
        }
    }

    //12.2.5.62 Between DOCTYPE public and system identifiers state
    private void inBetweenDoctypePublicAndSystemIdentifiers() {
        debug("inBetweenDoctypePublicAndSystemIdentifiers");
        // Consume the next input character:
        // EOF:
        if (position == length) {
            // EOF: This is an eof-in-doctype parse error.
            log.error("eof-in-doctype");
            // Set the DOCTYPE token's force-quirks flag to on. Emit that DOCTYPE token. Emit an end-of-file token.
            doctypeToken.setForceQuirks(true);
            tokens.add(doctypeToken);
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
            // U+0022 QUOTATION MARK (")
            case '"':
                // Set the DOCTYPE token's system identifier to the empty string (not missing),
                doctypeToken.setSystemIdentifier("");
                // then switch to the DOCTYPE system identifier (double-quoted) state.
                state = State.DOCTYPE_SYSTEM_IDENTIFIER_DOUBLE_QUOTED;
                position++;
                return;
            // U+0027 APOSTROPHE (')
            case '\'':
                // Set the DOCTYPE token's system identifier to the empty string (not missing),
                doctypeToken.setSystemIdentifier("");
                // then switch to the DOCTYPE system identifier (single-quoted) state.
                state = State.DOCTYPE_SYSTEM_IDENTIFIER_SINGLE_QUOTED;
                position++;
                return;
            // U+003E GREATER-THAN SIGN (>)
            case '>':
                // Switch to the data state.
                state = State.DATA;
                // Emit the current DOCTYPE token.
                tokens.add(doctypeToken);
                position++;
                return;
            default:
                // Anything else: This is a missing-quote-before-doctype-system-identifier parse error.
                log.error("missing-quote-before-doctype-system-identifier");
                // Set the DOCTYPE token's force-quirks flag to on.
                doctypeToken.setForceQuirks(true);
                // Reconsume in the bogus DOCTYPE state.
                state = State.BOGUS_DOCTYPE;
                break;
        }
    }

    //12.2.5.63 After DOCTYPE system keyword state
    private void inAfterDoctypeSystemKeyword() {
        debug("inAfterDoctypeSystemKeyword");
        // Consume the next input character:
        // EOF:
        if (position == length) {
            // EOF: This is an eof-in-doctype parse error.
            log.error("eof-in-doctype");
            // Set the DOCTYPE token's force-quirks flag to on. Emit that DOCTYPE token. Emit an end-of-file token.
            doctypeToken.setForceQuirks(true);
            tokens.add(doctypeToken);
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
                // Switch to the before DOCTYPE system identifier state.
                state = State.BEFORE_DOCTYPE_SYSTEM_IDENTIFIER;
                position++;
                break;
            // U+0022 QUOTATION MARK (")
            case '"':
                // This is a missing-whitespace-after-doctype-public-keyword parse error.
                log.error("missing-whitespace-after-doctype-public-keyword");
                // Set the DOCTYPE token's public identifier to the empty string (not missing),
                doctypeToken.setSystemIdentifier("");
                // then switch to the DOCTYPE system identifier (double-quoted) state.
                state = State.DOCTYPE_SYSTEM_IDENTIFIER_DOUBLE_QUOTED;
                position++;
                return;
            // U+0027 APOSTROPHE (')
            case '\'':
                // This is a missing-whitespace-after-doctype-public-keyword parse error.
                log.error("missing-whitespace-after-doctype-public-keyword");
                // Set the DOCTYPE token's system identifier to the empty string (not missing),
                doctypeToken.setSystemIdentifier("");
                // then switch to the DOCTYPE system identifier (single-quoted) state.
                state = State.DOCTYPE_SYSTEM_IDENTIFIER_SINGLE_QUOTED;
                position++;
                return;
            // U+003E GREATER-THAN SIGN (>)
            case '>':
                // This is a missing-doctype-public-identifier parse error.
                log.error("missing-doctype-public-identifier");
                // Set the DOCTYPE token's force-quirks flag to on. Switch to the data state. Emit that DOCTYPE token.
                doctypeToken.setForceQuirks(true);
                state = State.DATA;
                // Emit the current DOCTYPE token.
                tokens.add(doctypeToken);
                position++;
                return;
            default:
                // Anything else: This is a missing-quote-before-doctype-system-identifier parse error.
                log.error("missing-quote-before-doctype-system-identifier");
                // Set the DOCTYPE token's force-quirks flag to on.
                doctypeToken.setForceQuirks(true);
                // Reconsume in the bogus DOCTYPE state.
                state = State.BOGUS_DOCTYPE;
                break;
        }
    }

    //12.2.5.64 Before DOCTYPE system identifier state
    private void inBeforeDoctypeSystemIdentifier() {
        debug("inBeforeDoctypeSystemIdentifier");
        // Consume the next input character:
        // EOF:
        if (position == length) {
            // EOF: This is an eof-in-doctype parse error.
            log.error("eof-in-doctype");
            // Set the DOCTYPE token's force-quirks flag to on. Emit that DOCTYPE token. Emit an end-of-file token.
            doctypeToken.setForceQuirks(true);
            tokens.add(doctypeToken);
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
            // U+0022 QUOTATION MARK (")
            case '"':
                // Set the DOCTYPE token's system identifier to the empty string (not missing),
                doctypeToken.setSystemIdentifier("");
                // then switch to the DOCTYPE system identifier (double-quoted) state.
                state = State.DOCTYPE_SYSTEM_IDENTIFIER_DOUBLE_QUOTED;
                position++;
                return;
            // U+0027 APOSTROPHE (')
            case '\'':
                // Set the DOCTYPE token's system identifier to the empty string (not missing),
                doctypeToken.setSystemIdentifier("");
                // then switch to the DOCTYPE system identifier (single-quoted) state.
                state = State.DOCTYPE_SYSTEM_IDENTIFIER_SINGLE_QUOTED;
                position++;
                return;
            // U+003E GREATER-THAN SIGN (>)
            case '>':
                // This is a missing-doctype-system-identifier parse error.
                log.error("missing-doctype-system-identifier");
                // Set the DOCTYPE token's force-quirks flag to on. Switch to the data state. Emit that DOCTYPE token.
                doctypeToken.setForceQuirks(true);
                state = State.DATA;
                // Emit the current DOCTYPE token.
                tokens.add(doctypeToken);
                position++;
                return;
            default:
                // Anything else: This is a missing-quote-before-doctype-system-identifier parse error.
                log.error("missing-quote-before-doctype-system-identifier");
                // Set the DOCTYPE token's force-quirks flag to on.
                doctypeToken.setForceQuirks(true);
                // Reconsume in the bogus DOCTYPE state.
                state = State.BOGUS_DOCTYPE;
                break;
        }
    }

    //12.2.5.65 DOCTYPE system identifier (double-quoted) state
    private void inDoctypeSystemIdentifierDoubleQuoted() {
        debug("inDoctypeSystemIdentifierDoubleQuoted");
        // Consume the next input character:
        // EOF:
        if (position == length) {
            // EOF: This is an eof-in-doctype parse error.
            log.error("eof-in-doctype");
            // Set the DOCTYPE token's force-quirks flag to on. Emit that DOCTYPE token. Emit an end-of-file token.
            doctypeToken.setForceQuirks(true);
            tokens.add(doctypeToken);
            tokens.add(new EndOfFileToken());
            position++;
            return;
        }
        char c = htmlString.charAt(position);
        switch (c) {
            // U+0022 QUOTATION MARK (")
            case '"':
                // Switch to the after DOCTYPE system identifier state.
                state = State.AFTER_DOCTYPE_SYSTEM_IDENTIFIER;
                position++;
                return;
            // U+0000 NULL
            case 0x0000:
                // This is an unexpected-null-character parse error.
                log.error("unexpected-null-character");
                // Append a U+FFFD REPLACEMENT CHARACTER character to the current DOCTYPE token's system identifier.
                doctypeToken.setSystemIdentifier(doctypeToken.getSystemIdentifier() + (char) 0xFFFD);
                position++;
                return;
            // U+003E GREATER-THAN SIGN (>)
            case '>':
                // This is an abrupt-doctype-system-identifier parse error.
                log.error("abrupt-doctype-system-identifier");
                // Set the DOCTYPE token's force-quirks flag to on. Switch to the data state. Emit that DOCTYPE token.
                doctypeToken.setForceQuirks(true);
                state = State.DATA;
                tokens.add(doctypeToken);
                position++;
                return;
            default:
                // Anything else: Append the current input character to the current DOCTYPE token's system identifier.
                doctypeToken.setSystemIdentifier(doctypeToken.getSystemIdentifier() + c);
                position++;
                break;
        }
    }

    //12.2.5.66 DOCTYPE system identifier (single-quoted) state
    private void inDoctypeSystemIdentifierSingleQuoted() {
        debug("inDoctypeSystemIdentifierSingleQuoted");
        // Consume the next input character:
        // EOF:
        if (position == length) {
            // EOF: This is an eof-in-doctype parse error.
            log.error("eof-in-doctype");
            // Set the DOCTYPE token's force-quirks flag to on. Emit that DOCTYPE token. Emit an end-of-file token.
            doctypeToken.setForceQuirks(true);
            tokens.add(doctypeToken);
            tokens.add(new EndOfFileToken());
            position++;
            return;
        }
        char c = htmlString.charAt(position);
        switch (c) {
            // U+0027 APOSTROPHE (')
            case '\'':
                // Switch to the after DOCTYPE public identifier state.
                state = State.AFTER_DOCTYPE_SYSTEM_IDENTIFIER;
                position++;
                return;
            // U+0000 NULL
            case 0x0000:
                // This is an unexpected-null-character parse error.
                log.error("unexpected-null-character");
                // Append a U+FFFD REPLACEMENT CHARACTER character to the current DOCTYPE token's system identifier.
                doctypeToken.setSystemIdentifier(doctypeToken.getSystemIdentifier() + (char) 0xFFFD);
                position++;
                return;
            // U+003E GREATER-THAN SIGN (>)
            case '>':
                // This is an abrupt-doctype-system-identifier parse error.
                log.error("abrupt-doctype-system-identifier");
                // Set the DOCTYPE token's force-quirks flag to on. Switch to the data state. Emit that DOCTYPE token.
                doctypeToken.setForceQuirks(true);
                state = State.DATA;
                tokens.add(doctypeToken);
                position++;
                return;
            default:
                // Anything else: Append the current input character to the current DOCTYPE token's system identifier.
                doctypeToken.setSystemIdentifier(doctypeToken.getSystemIdentifier() + c);
                position++;
                break;
        }
    }

    //12.2.5.67 After DOCTYPE system identifier state
    private void inAfterDoctypeSystemIdentifier() {
        debug("inAfterDoctypeSystemIdentifier");
        // Consume the next input character:
        // EOF:
        if (position == length) {
            // EOF: This is an eof-in-doctype parse error.
            log.error("eof-in-doctype");
            // Set the DOCTYPE token's force-quirks flag to on. Emit that DOCTYPE token. Emit an end-of-file token.
            doctypeToken.setForceQuirks(true);
            tokens.add(doctypeToken);
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
            // U+003E GREATER-THAN SIGN (>)
            case '>':
                // Switch to the data state. Emit the current DOCTYPE token.
                state = State.DATA;
                tokens.add(doctypeToken);
                position++;
                return;
            default:
                // Anything else: This is a unexpected-character-after-doctype-system-identifier parse error.
                log.error("unexpected-character-after-doctype-system-identifier");
                // Reconsume in the bogus DOCTYPE state. (This does not set the DOCTYPE token's force-quirks flag to on.)
                state = State.BOGUS_DOCTYPE;
                break;
        }
    }

    //12.2.5.68 Bogus DOCTYPE state
    private void inBogusDoctype() {
        debug("inBogusDoctype");
        // Consume the next input character:
        // EOF:
        if (position == length) {
            // Emit that DOCTYPE token. Emit an end-of-file token.
            tokens.add(doctypeToken);
            tokens.add(new EndOfFileToken());
            position++;
            return;
        }
        char c = htmlString.charAt(position);
        switch (c) {
            // U+0000 NULL
            case 0x0000:
                // This is an unexpected-null-character parse error.
                log.error("unexpected-null-character");
                // Ignore the character.
                position++;
                return;
            // U+003E GREATER-THAN SIGN (>)
            case '>':
                // Switch to the data state. Emit that DOCTYPE token.
                state = State.DATA;
                tokens.add(doctypeToken);
                position++;
                return;
            default:
                // Anything else: Ignore the character.
                position++;
                break;
        }
    }

    //12.2.5.69 CDATA section state
    private void inCDataSection() {
        debug("inCDataSection");
        // Consume the next input character:
        // EOF:
        if (position == length) {
            // This is an eof-in-cdata parse error.
            log.error("eof-in-cdata");
            // Emit an end-of-file token.
            tokens.add(new EndOfFileToken());
            position++;
            return;
        }
        char c = htmlString.charAt(position);
        switch (c) {
            // U+005D RIGHT SQUARE BRACKET (])
            case ']':
                // Switch to the CDATA section bracket state.
                state = State.CDATA_SECTION_BRACKET;
                position++;
                return;
            default:
                // Anything else: Emit the current input character as a character token.
                tokens.add(new CharacterToken(c));
                position++;
                break;
        }
    }

    //12.2.5.70 CDATA section bracket state
    private void inCDataSectionBracket() {
        debug("inCDataSectionBracket");
        // Consume the next input character:
        char c = htmlString.charAt(position);
        switch (c) {
            // U+005D RIGHT SQUARE BRACKET (])
            case ']':
                // Switch to the CDATA section end state.
                state = State.CDATA_SECTION_END;
                position++;
                return;
            default:
                // Emit a U+005D RIGHT SQUARE BRACKET character token.
                tokens.add(new CharacterToken(']'));
                // Reconsume in the CDATA section state.
                state = State.CDATA_SECTION;
                break;
        }
    }

    //12.2.5.71 CDATA section end state
    private void inCDataSectionEnd() {
        debug("inCDataSectionEnd");
        // Consume the next input character:
        char c = htmlString.charAt(position);
        switch (c) {
            // U+005D RIGHT SQUARE BRACKET (])
            case ']':
                // Emit a U+005D RIGHT SQUARE BRACKET character token.
                tokens.add(new CharacterToken(']'));
                position++;
                return;
            // U+003E GREATER-THAN SIGN character
            case 0x003E:
                // Switch to the data state.
                state = State.DATA;
                position++;
                return;
            default:
                // Emit two U+005D RIGHT SQUARE BRACKET character tokens.
                tokens.add(new CharacterToken(']'));
                tokens.add(new CharacterToken(']'));
                // Reconsume in the CDATA section state.
                state = State.CDATA_SECTION;
                break;
        }
    }

    //12.2.5.72 Character reference state
    private void inCharacterReferenceState() {
        debug("inCharacterReferenceState");
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


    private void debug(String methodName) {
        String s = doctypeToken == null ? "" : doctypeToken.getName();
        log.info(String.format("{ %s } %s %s", substring(16), methodName, s));
    }


    //TODO in functions
    private String substring(int length) {
        return substring(htmlString, position, length);
    }

    private String substring(String source, int beginIndex, int length) {
        if (source == null) {
            return null;
        }
        if (beginIndex > source.length()) {
            return "";
        }
        if (beginIndex + length > source.length()) {
            return source.substring(beginIndex);
        }
        return source.substring(beginIndex, beginIndex + length);
    }
}
