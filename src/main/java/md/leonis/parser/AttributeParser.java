package md.leonis.parser;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import md.leonis.parser.domain.Attribute;

@Slf4j
@Getter
class AttributeParser {

    private String asciiHtmlString;
    private int position;

    // Let attribute name and attribute value be the empty string.
    Attribute attribute = new Attribute("", "");

    AttributeParser(String asciiHtmlString, int position) {
        this.asciiHtmlString = asciiHtmlString;
        this.position = position;
    }

    Attribute getAttribute() {
        while (position < asciiHtmlString.length()) {
            log.info("    [" + substring(16) + "] (" + position + ")");
            char character = asciiHtmlString.charAt(position);
            // 1. If the byte at position is one of 0x09 (HT), 0x0A (LF), 0x0C (FF),
            // 0x0D (CR), 0x20 (SP), or 0x2F (/) then
            // advance position to the next byte and redo this step.
            if (character == 0x09 || character == 0x0A || character == 0x0C || character == 0x0D || character == 0x20 ||
                    character == 0x2F) {
                position++;
            } else
                // 2. If the byte at position is 0x3E (>),
                // then abort the get an attribute algorithm. There isn't one.
                if (character == '>') {
                    return null;
                }
                // 3. Otherwise, the byte at position is the start of the attribute name.
                else {
                    return processAttribute();
                }
        }
        return null;
    }

    // 4. Process the byte at position as follows:
    private Attribute processAttribute() {
        while (position < asciiHtmlString.length()) {
            log.info("    [" + substring(16) + "] (" + position + ")");
            char character = asciiHtmlString.charAt(position);
            // If it is 0x3D (=), and the attribute name is longer than the empty string
            if ('=' == character && !attribute.getName().isEmpty()) {
                // Advance position to the next byte and jump to the step below labeled value.
                position++;
                processValue();
                return attribute;
            } else
                // If it is 0x09 (HT), 0x0A (LF), 0x0C (FF), 0x0D (CR), or 0x20 (SP)
                if (0x09 == character || 0x0A == character || 0x0C == character
                        || 0x0D == character || 0x20 == character) {
                    // Jump to the step below labeled spaces.
                    if (processSpaces()) {
                        return attribute;
                    }
                } else
                    // If it is 0x2F (/) or 0x3E (>)
                    if (0x2F == character || 0x3E == character) {
                        // Abort the get an attribute algorithm. The attribute's name is the value
                        // of attribute name, its value is the empty string.
                        return attribute;
                    }
                    // If it is in the range 0x41 (A) to 0x5A (Z)
                    // Append the code point b+0x20 to attribute name (where b is the value
                    // of the byte at position). (This converts the input to lowercase.)

                    // Anything else
                    // Append the code point with the same value as the byte at position to
                    // attribute name. (It doesn't actually matter how bytes outside the ASCII
                    // range are handled here, since only ASCII bytes can contribute to the
                    // detection of a character encoding.)
                    else {
                        attribute.setName(attribute.getName() + character);
                    }
            // 5. Advance position to the next byte and return to the previous step.
            position++;
        }
        return attribute;
    }

    private boolean processSpaces() {
        // 6. Spaces: If the byte at position is one of 0x09 (HT), 0x0A (LF), 0x0C (FF),
        // 0x0D (CR), or 0x20 (SP) then advance position to the next byte, then, repeat this step.
        if (skipSpaces()) {
            return true;
        } else {
            // 7. If the byte at position is not 0x3D (=), abort the get an attribute
            // algorithm. The attribute's name is the value of attribute name,
            // its value is the empty string.
            char character = asciiHtmlString.charAt(position);
            if ('=' != character) {
                return true;
            } else {
                // 8. Advance position past the 0x3D (=) byte.
                position++;
                return false;
            }
        }
    }

    private boolean skipSpaces() {
        while (position < asciiHtmlString.length()) {
            log.info("    [" + substring(16) + "] (" + position + ")");
            // If the byte at position is one of 0x09 (HT), 0x0A (LF), 0x0C (FF),
            // 0x0D (CR), or 0x20 (SP) then advance position to the next byte,
            // then, repeat this step.
            char character = asciiHtmlString.charAt(position);
            if (0x09 == character || 0x0A == character || 0x0C == character
                    || 0x0D == character || 0x20 == character) {
                position++;
            } else {
                return false;
            }
        }
        return true;
    }

    private void processValue() {
        // 9. Value: If the byte at position is one of 0x09 (HT), 0x0A (LF),
        // 0x0C (FF), 0x0D (CR), or 0x20 (SP) then advance position to the next byte,
        // then, repeat this step.
        if (!skipSpaces()) {
            // 10. Process the byte at position as follows:
            // If it is 0x22 (") or 0x27 (')
            char character = asciiHtmlString.charAt(position);
            if ('"' == character || '\'' == character) {
                // 1. Let b be the value of the byte at position.
                char b = character;
                while (position < asciiHtmlString.length()) {
                    log.info("    [" + substring(16) + "] (" + position + ")");
                    // 2. Quote loop: Advance position to the next byte.
                    position++;
                    character = asciiHtmlString.charAt(position);
                    // 3. If the value of the byte at position is the value of b,
                    // then advance position to the next byte and abort the
                    // "get an attribute" algorithm.
                    // The attribute's name is the value of attribute name,
                    // and its value is the value of attribute value.
                    if (character == b) {
                        position++;
                        break;
                    } else
                        // 4. Otherwise, if the value of the byte at position is in the
                        // range 0x41 (A) to 0x5A (Z), then append a code point to
                        // attribute value whose value is 0x20 more than the value
                        // of the byte at position.

                        // 5. Otherwise, append a code point to attribute value whose
                        // value is the same as the value of the byte at position.
                        attribute.setValue(attribute.getValue() + character);
                    // 6. Return to the step above labeled quote loop.
                }
                return;
            } else
                // If it is 0x3E (>)
                if ('>' == character) {
                    // Abort the get an attribute algorithm. The attribute's name is the
                    // value of attribute name, its value is the empty string.
                    return;
                } else {
                    // If it is in the range 0x41 (A) to 0x5A (Z)
                    //  Append a code point b+0x20 to attribute value (where b is the value
                    // of the byte at position). Advance position to the next byte.

                    //  Anything else
                    // Append a code point with the same value as the byte at position to
                    // attribute value. Advance position to the next byte.
                    attribute.setValue(attribute.getValue() + character);
                }
            // Process the byte at position as follows:
            while (position < asciiHtmlString.length()) {
                log.info("    [" + substring(16) + "] (" + position + ")");
                character = asciiHtmlString.charAt(position);
                // If it is 0x09 (HT), 0x0A (LF), 0x0C (FF), 0x0D (CR), 0x20 (SP), or 0x3E (>)
                if (0x09 == character || 0x0A == character || 0x0C == character
                        || 0x0D == character || 0x20 == character || 0x3E == character) {
                    // Abort the get an attribute algorithm. The attribute's name is the
                    // value of attribute name and its value is the value of attribute value.
                    return;
                } else {
                    // If it is in the range 0x41 (A) to 0x5A (Z)
                    //  Append a code point b+0x20 to attribute value (where b is the value
                    // of the byte at position).

                    // Anything else
                    // Append a code point with the same value as the byte at position to
                    // attribute value.
                    attribute.setValue(attribute.getValue() + character);
                }
                // Advance position to the next byte and return to the previous step.
                position++;
            }
        }
    }

    private String substring(int length) {
        return substring(asciiHtmlString, position, length);
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




















