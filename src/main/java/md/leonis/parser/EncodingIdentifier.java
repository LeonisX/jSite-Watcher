package md.leonis.parser;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import md.leonis.parser.domain.Attribute;
import md.leonis.parser.domain.Confidence;
import md.leonis.parser.domain.PageEncoding;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Getter
@Setter
class EncodingIdentifier {

    private byte[] html;
    private String asciiHtmlString;
    private int position;

    static PageEncoding detectCharset(byte[] html) {
        EncodingIdentifier encodingIdentifier = new EncodingIdentifier();
        encodingIdentifier.setHtml(html);
        encodingIdentifier.setAsciiHtmlString(new String(html, 0, html.length > 1024 ? 1024 : html.length, StandardCharsets.ISO_8859_1).toLowerCase());

        // TODO 4 prescan
        log.info("prescan");
        PageEncoding pageEncoding = encodingIdentifier.doPrescan();
        log.info("encoding after prescan: " + pageEncoding);
        if (pageEncoding != null) {
            return pageEncoding;
        }

        // TODO 5 iframe
        // TODO 6 is previously saved encoding >> TENTATIVE
        // TODO 7 autodetect
        // TODO 8 default (predefined) or locale >> TENTATIVE
        return new PageEncoding("", Confidence.TENTATIVE);
    }

/*
    IFRAME:

    If the HTML parser for which this algorithm is being run is associated with a Document that is itself in a nested browsing context, run these substeps:

    Let new document be the Document with which the HTML parser is associated.

    Let parent document be the Document through which new document is nested (the active document of the parent browsing context of new document).

    If parent document's origin is not the same origin as new document's origin, then abort these substeps.

    If parent document's character encoding is not an ASCII-compatible encoding, then abort these substeps.

    Return parent document's character encoding, with the confidence tentative, and abort the encoding sniffing algorithm's steps.

    Otherwise, if the user agent has information on the likely encoding for this page, e.g. based on the encoding of the page when it was last visited, then return that encoding, with the confidence tentative, and abort these steps.
*/


    private PageEncoding doPrescan() {
        // 1. Let position be a pointer to a byte in the input byte stream,
        // initially pointing at the first byte.
        position = 0;
        // 2. Loop: If position points to:
        while (position < asciiHtmlString.length()) {
            log.info("    [" + substring(16) + "] (" + position + ")");
            // A sequence of bytes starting with: 0x3C 0x21 0x2D 0x2D (`<!--`)
            if (isCommentStart()) {
                log.info("CommentStart");
                // Advance the position pointer so that it points at the first 0x3E byte
                // which is preceded by two 0x2D bytes (i.e. at the end of an ASCII '-->'
                // sequence) and comes after the 0x3C byte that was found.
                // (The two 0x2D bytes can be the same as those in the '<!--' sequence.)
                findCommentEnd();
            } else
                // A sequence of bytes starting with: 0x3C, 0x4D or 0x6D,
                // 0x45 or 0x65, 0x54 or 0x74, 0x41 or 0x61,
                // and one of 0x09, 0x0A, 0x0C, 0x0D, 0x20, 0x2F
                // (case-insensitive ASCII '<meta' followed by a space or slash)
                if (isMetaStart()) {
                    log.info("MetaStart");
                    PageEncoding encoding = processMeta();
                    if (encoding != null) {
                        return encoding;
                    }
                } else
                    // A sequence of bytes starting with a 0x3C byte (<), optionally a 0x2F byte (/),
                    // and finally a byte in the range 0x41-0x5A or 0x61-0x7A (A-Z or a-z)
                    if (isSpaceOrOpeningBrace()) {
                        log.info("SpaceOrClosingBrace");
                        // 1. Advance the position pointer so that it points at the next
                        // 0x09 (HT), 0x0A (LF), 0x0C (FF), 0x0D (CR), 0x20 (SP),
                        // or 0x3E (>) byte.

                        // 2. Repeatedly get an attribute until no further attributes
                        // can be found, then jump to the step below labeled next byte.
                        processSpaceOrOpeningBrace();
                    } else {
                        // A sequence of bytes starting with: 0x3C 0x21 (`<!`)
                        // A sequence of bytes starting with: 0x3C 0x2F (`</`)
                        // A sequence of bytes starting with: 0x3C 0x3F (`<?`)
                        String otherTagStart = substring(2);
                        //TODO optimize
                        List<String> tagStarts = Arrays.asList("<!", "</", "<?");
                        if (tagStarts.contains(otherTagStart)) {
                            log.info("OtherTagOpen");
                            // Advance the position pointer so that it points at the first 0x3E byte (>)
                            // that comes after the 0x3C byte that was found.
                            position++;
                            findClosingBrace();
                        }
                    }
            // Any other byte: do nothing with that byte.

            // Next byte: Move position so it points at the next byte in the input byte stream,
            // and return to the step above labeled loop.
            position++;
        }
        return null;
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

    // Advance the position pointer so that it points at the first 0x3E byte (>)
    // that comes after the 0x3C byte that was found.
    private void findClosingBrace() {
        while (position < html.length) {
            log.info("    [" + substring(16) + "] (" + position + ")");
            if ('>' == asciiHtmlString.charAt(position)) {
                return;
            }
            position++;
        }
    }

    // A sequence of bytes starting with a 0x3C byte (<), optionally a 0x2F byte (/),
    // and finally a byte in the range 0x41-0x5A or 0x61-0x7A (A-Z or a-z)
    private boolean isSpaceOrOpeningBrace() {
        if (position < html.length) {
            if ('<' == asciiHtmlString.charAt(position)) {
                char secondChar = asciiHtmlString.charAt(position + 1);
                if (position + 1 >= html.length) {
                    return false;
                }
                // 1. Advance the position pointer so that it points at the next 0x09 (HT),
                // 0x0A (LF), 0x0C (FF), 0x0D (CR), 0x20 (SP), or 0x3E (>) byte.
                if (Character.isLetter(secondChar)) {
                    position += 2;
                    return true;
                }
                if (position + 2 >= html.length) {
                    return false;
                }
                char thirdChar = asciiHtmlString.charAt(position + 2);
                if ('/' == secondChar && Character.isLetter(thirdChar)) {
                    position += 3;
                    return true;
                }
            }
        }
        return false;
    }

    // 2. Repeatedly get an attribute until no further attributes can be found,
    // then jump to the step below labeled next byte.
    private void processSpaceOrOpeningBrace() {
        while (true) {
            AttributeParser attributeParser = new AttributeParser(asciiHtmlString, position);
            Attribute attribute = attributeParser.getAttribute();
            position = attributeParser.getPosition();
            if (attribute == null) {
                return;
            }
        }
    }


    private PageEncoding processMeta() {
        // 1. Advance the position pointer so that it points at the
        // next 0x09, 0x0A, 0x0C, 0x0D, 0x20, or 0x2F byte
        // (the one in sequence of characters matched above).
        position += 5;
        // 2. Let attribute list be an empty list of strings.
        Map<String, String> attributes = new HashMap<>();
        // 3. Let got pragma be false.
        boolean gotPragma = false;
        // 4. Let need pragma be null.
        Boolean needPragma = null;
        // 5. Let charset be the null value (which, for the purposes of this algorithm,
        // is distinct from an unrecognized encoding or the empty string).
        String charset = null;
        // TODO limit
        while (true) {
            // 6. Attributes: Get an attribute and its value. If no attribute was sniffed,
            // then jump to the processing step below.
            AttributeParser attributeParser = new AttributeParser(asciiHtmlString, position);
            Attribute attribute = attributeParser.getAttribute();
            position = attributeParser.getPosition();
            if (attribute == null) {
                // 11. Processing: If need pragma is null,
                // then jump to the step below labeled next byte.
                //TODO may be need hack - remove this condition
                if (null == needPragma) {
                    return null;
                } else
                // 12. If need pragma is true but got pragma is false,
                // then jump to the step below labeled next byte.
                    //TODO may be need hack - remove this condition
                if (needPragma && !gotPragma) {
                    return null;
                }
                // 13. If charset is failure,
                // then jump to the step below labeled next byte.
                if (null == charset){
                    return null;
                }
                // 14. If charset is a UTF-16 encoding, then set charset to UTF-8.
                if (charset.equals("utf-16")) {
                    charset = "utf-8";
                }
                // 15.If charset is x-user-defined, then set charset to windows-1252.
                if (charset.equals("x-user-defined")) {
                    charset = "windows-1252";
                }
                // 16. Abort the prescan a byte stream to determine its encoding algorithm,
                // returning the encoding given by charset.
                return new PageEncoding(charset, Confidence.TENTATIVE);
            } else if (!attributes.containsKey(attribute.getName())) {
                // 8. Add the attribute's name to attribute list.
                attributes.put(attribute.getName(), attribute.getValue());
                // 9. Run the appropriate step from the following list, if one applies:
                //    - If the attribute's name is "http-equiv"
                if ("http-equiv".equals(attribute.getName())) {
                    // If the attribute's value is "content-type",
                    // then set got pragma to true.
                    if ("content-type".equals(attribute.getValue())) {
                        gotPragma = true;
                    }
                }
                //    - If the attribute's name is "content"
                if ("content".equals(attribute.getName())) {
                    // Apply the algorithm for extracting a character encoding from a
                    // meta element, giving the attribute's value as the string to parse.
                    String encoding = processContent(attribute.getValue());
                    // If a character encoding is returned, and if charset is still set to null,
                    // let charset be the encoding returned, and set need pragma to true.
                    if (encoding != null && charset == null) {
                        charset = encoding;
                        needPragma = true;
                    }
                }
                //    - If the attribute's name is "charset"
                if ("charset".equals(attribute.getName())) {
                    // Let charset be the result of getting an encoding
                    // from the attribute's value, and set need pragma to false.
                    charset = attribute.getValue();
                    needPragma = false;
                }
                // 7. If the attribute's name is already in attribute list,
                // then return to the step labeled attributes.
                // 10. Return to the step labeled attributes.
            }
        }
    }

    // TODO not ideal algorithm
    private String processContent(String value) {
        // 1. Let position be a pointer into s, initially pointing at the start of the string.

        // 2. Loop: Find the first seven characters in s after position that are an
        // ASCII case-insensitive match for the word "charset". If no such match is found,
        // return nothing and abort these steps.
        int pos = 0;
        while (pos < value.length()) {
            log.info("    [" + substring(16) + "] (" + pos + ")");
            pos = value.indexOf("charset", pos);
            if (pos < 0) {
                return null;
            }
            pos = pos + 7;
            // 3. Skip any ASCII whitespace that immediately follow the word "charset"
            // (there might not be any).
            pos = skipSpaces(value, pos);
            // 4. If the next character is not a U+003D EQUALS SIGN (=),
            // then move position to point just before that next character,
            // and jump back to the step labeled loop.
            if (value.charAt(pos) == '=') {
                pos++;
                break;
            }
            pos++;
        }
        // 5. Skip any ASCII whitespace that immediately follow
        // the equals sign (there might not be any).
        pos = skipSpaces(value, pos);
        // 6. Process the next character as follows:

        //    If it is a U+0022 QUOTATION MARK character (") and there
        //    is a later U+0022 QUOTATION MARK character (") in s

        //    If it is a U+0027 APOSTROPHE character (') and there is a
        //    later U+0027 APOSTROPHE character (') in s
        char b = value.charAt(pos);
        if ('"' != b && '\'' != b) {
            b = 0;
        } else {
            pos++;
        }
        //       Return the result of getting an encoding from the substring that is between
        //       this character and the next earliest occurrence of this character.

        //    If it is an unmatched U+0022 QUOTATION MARK character (")
        //    If it is an unmatched U+0027 APOSTROPHE character (')
        //    If there is no next character
        //       Return nothing.

        //    Otherwise
        //       Return the result of getting an encoding from the substring that consists
        //       of this character up to but not including the first ASCII whitespace or
        //       U+003B SEMICOLON character (;), or the end of s, whichever comes first.
        StringBuilder result = new StringBuilder();
        while (pos < value.length()) {
            log.info("    [" + substring(16) + "] (" + pos + ")");
            char character = value.charAt(pos);
            if (0x09 == character || 0x0A == character || 0x0C == character
                    || 0x0D == character || 0x20 == character || ';' == character || b == character) {
                return result.toString();
            } else {
                result.append(character);
                pos++;
            }
        }
        if (pos == value.length() && b != 0) {
            return null;
        }
        return result.toString();
        // This algorithm is distinct from those in the HTTP specification
        // (for example, HTTP doesn't allow the use of single quotes and requires supporting
        // a backslash-escape mechanism that is not supported by this algorithm).
        // While the algorithm is used in contexts that, historically, were related to HTTP,
        // the syntax as supported by implementations diverged some time ago.
    }

    private int skipSpaces(String value, int pos) {
        log.info("Skip spaces");
        while (pos < value.length()) {
            log.info("    [" + substring(16) + "] (" + pos + ")");
            // If the byte at position is one of 0x09 (HT), 0x0A (LF), 0x0C (FF),
            // 0x0D (CR), or 0x20 (SP) then advance position to the next byte,
            // then, repeat this step.
            char character = value.charAt(pos);
            if (0x09 == character || 0x0A == character || 0x0C == character
                    || 0x0D == character || 0x20 == character) {
                pos++;
            } else {
                return pos;
            }
        }
        return pos;
    }

    private boolean isMetaStart() {
        // A sequence of bytes starting with: 0x3C, 0x4D or 0x6D, 0x45 or 0x65, 0x54 or 0x74, 0x41 or 0x61,
        // and one of 0x09, 0x0A, 0x0C, 0x0D, 0x20, 0x2F (case-insensitive ASCII '<meta' followed by a space or slash)
        //TODO const
        Pattern metaPattern = Pattern.compile("^<meta[\\x09\\x0A\\x0C\\x0D\\x20\\x2F]$");
        String chunk = substring(6);
        Matcher matcher = metaPattern.matcher(chunk);
        return (matcher.matches());
    }


    private boolean isCommentStart() {
        // A sequence of bytes starting with: 0x3C 0x21 0x2D 0x2D (`<!--`)
        String chunk = substring(4);
        return ("<!--".equals(chunk));
    }

    private void findCommentEnd() {
        position += 2;
        // Advance the position pointer so that it points at the first 0x3E byte which is preceded by
        // two 0x2D bytes (i.e. at the end of an ASCII '-->' sequence) and comes after the 0x3C byte that was found.
        // (The two 0x2D bytes can be the same as those in the '<!--' sequence.)
        int pos = asciiHtmlString.indexOf("-->", position);
        if (pos == -1) {
            position = asciiHtmlString.length();
        } else {
            position = pos + 2;
        }
    }


}
