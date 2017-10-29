package md.leonis.parser;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
class InputStreamPreprocessor {

    private String htmlString;

    String process() {
        for (int offset = 0; offset < htmlString.length(); ) {
            final int codePoint = htmlString.codePointAt(offset);
            // Any occurrences of surrogates are surrogate-in-input-stream parse errors.
            if (isSurrogate(codePoint)) {
                // This error occurs if the input stream contains a surrogate.
                // Such code points are parsed as-is and usually, where parsing rules don't apply
                // any additional restrictions, make their way into the DOM.
                // Surrogates can only find their way into the input stream via script APIs such as document.write().
                log.error("surrogate-in-input-stream");
            }
            // Any occurrences of noncharacters are noncharacter-in-input-stream parse errors
            if (isNonCharacter(codePoint)) {
                // This error occurs if the input stream contains a noncharacter.
                // Such code points are parsed as-is and usually, where parsing rules don't apply any
                // additional restrictions, make their way into the DOM.
                log.error("noncharacter-in-input-stream");
            }
            // Any occurrences of controls other than ASCII whitespace and U+0000 NULL characters
            // are control-character-in-input-stream parse errors.
            if (isControlWoWhitespacesCharacter(codePoint)) {
                // This error occurs if the input stream contains a control code point that is not
                // ASCII whitespace or U+0000 NULL. Such code points are parsed as-is and usually,
                // where parsing rules don't apply any additional restrictions, make their way into the DOM.
                log.error("control-character-in-input-stream");
            }
            offset += Character.charCount(codePoint);
        }
        //U+000D CARRIAGE RETURN (CR) characters and U+000A LINE FEED (LF) characters are treated specially.
        // Any LF character that immediately follows a CR character must be ignored, and all CR characters
        // must then be converted to LF characters. Thus, newlines in HTML DOMs are represented by LF characters,
        // and there are never any CR characters in the input to the tokenization stage.
        //TODO optionally
        return htmlString.replace(Constants.CRLF, Constants.LF_STRING).replace(Constants.CR, Constants.LF);
    }

    // A surrogate is a code point that is in the range U+D800 to U+DFFF, inclusive.
    private boolean isSurrogate(int codepoint) {
        return codepoint >= Character.MIN_SURROGATE && codepoint <= Character.MAX_SURROGATE;
    }

    // A noncharacter is a code point that is in the range U+FDD0 to U+FDEF, inclusive, or U+FFFE, U+FFFF, U+1FFFE,
    // U+1FFFF, U+2FFFE, U+2FFFF, U+3FFFE, U+3FFFF, U+4FFFE, U+4FFFF, U+5FFFE, U+5FFFF, U+6FFFE, U+6FFFF, U+7FFFE,
    // U+7FFFF, U+8FFFE, U+8FFFF, U+9FFFE, U+9FFFF, U+AFFFE, U+AFFFF, U+BFFFE, U+BFFFF, U+CFFFE, U+CFFFF, U+DFFFE,
    // U+DFFFF, U+EFFFE, U+EFFFF, U+FFFFE, U+FFFFF, U+10FFFE, or U+10FFFF.
    private boolean isNonCharacter(int codepoint) {
        return Constants.NON_CHARACTERS_SET.contains(codepoint);
    }

    // Detect any occurrences of controls other than ASCII whitespace and U+0000 NULL characters
    // ASCII whitespace is U+0009 TAB, U+000A LF, U+000C FF, U+000D CR, or U+0020 SPACE.
    // A C0 control is a code point in the range U+0000 NULL to U+001F INFORMATION SEPARATOR ONE, inclusive.
    // A control is a C0 control or a code point in the range U+007F DELETE to U+009F APPLICATION PROGRAM COMMAND, inclusive.
    private boolean isControlWoWhitespacesCharacter(int codePoint) {
        return Constants.CONTROL_WO_WHITESPACES_SET.contains(codePoint);
    }

}
