package md.leonis.parser;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

class Constants {

    static final char CR = (char) 0x0D;
    static final char LF = (char) 0x0A;
    static final String LF_STRING = "" + LF;
    static final String CRLF = "" + CR + LF;

    // Not need A `surrogate` is a code point that is in the range U+D800 to U+DFFF, inclusive.

    // Not need A `scalar value` is a code point that is not a surrogate.

    // Not need An `ASCII code point` is a code point in the range U+0000 NULL to U+007F DELETE, inclusive.

    // TODO An `ASCII tab or newline` is U+0009 TAB, U+000A LF, or U+000D CR.


    // A `noncharacter` is a code point that is in the range U+FDD0 to U+FDEF, inclusive, or U+FFFE, U+FFFF, U+1FFFE,
    // U+1FFFF, U+2FFFE, U+2FFFF, U+3FFFE, U+3FFFF, U+4FFFE, U+4FFFF, U+5FFFE, U+5FFFF, U+6FFFE, U+6FFFF, U+7FFFE,
    // U+7FFFF, U+8FFFE, U+8FFFF, U+9FFFE, U+9FFFF, U+AFFFE, U+AFFFF, U+BFFFE, U+BFFFF, U+CFFFE, U+CFFFF, U+DFFFE,
    // U+DFFFF, U+EFFFE, U+EFFFF, U+FFFFE, U+FFFFF, U+10FFFE, or U+10FFFF.
    static final Set<Integer> NON_CHARACTERS_SET;

    // `ASCII whitespace` is U+0009 TAB, U+000A LF, U+000C FF, U+000D CR, or U+0020 SPACE.
    static final Set<Integer> ASCII_WHITESPACES_SET;

    // A `C0 control` is a code point in the range U+0000 NULL to U+001F INFORMATION SEPARATOR ONE, inclusive.
    static final Set<Integer> C0_CONTROL_SET;

    // TODO A C0 control or space is a C0 control or U+0020 SPACE.

    // A `control` is a C0 control or a code point in the range U+007F DELETE to U+009F APPLICATION PROGRAM COMMAND, inclusive.
    static final Set<Integer> CONTROL_SET;

    // Controls, other than ASCII whitespace and U+0000 NULL characters
    static final Set<Integer> CONTROL_WO_WHITESPACES_SET;

    // An `ASCII digit` is a code point in the range U+0030 (0) to U+0039 (9), inclusive.
    static final Set<Character> ASCII_DIGIT;

    // TODO An `ASCII upper hex digit` is an ASCII digit or a code point in the range U+0041 (A) to U+0046 (F), inclusive.

    // TODO An `ASCII lower hex digit` is an ASCII digit or a code point in the range U+0061 (a) to U+0066 (f), inclusive.

    // TODO An `ASCII hex digit` is an ASCII upper hex digit or ASCII lower hex digit.

    // An `ASCII upper alpha` is a code point in the range U+0041 (A) to U+005A (Z), inclusive.
    static final Set<Character> ASCII_UPPER_ALPHA;

    // An `ASCII lower alpha` is a code point in the range U+0061 (a) to U+007A (z), inclusive.
    static final Set<Character> ASCII_LOWER_ALPHA;

    // An `ASCII alpha` is an ASCII upper alpha or ASCII lower alpha.
    static final Set<Character> ASCII_ALPHA;

    // An `ASCII alphanumeric` is an ASCII digit or ASCII alpha.
    static final Set<Character> ASCII_ALPHANUMERIC;

    static {
        Set<Integer> codePointsSet = new HashSet<>();
        for (Integer i = 0xFDD0; i <= 0xFDEF; i++) {
            codePointsSet.add(i);
        }
        Collections.addAll(codePointsSet, 0xFFFE, 0xFFFF, 0x1FFFE,
                0x1FFFF, 0x2FFFE, 0x2FFFF, 0x3FFFE, 0x3FFFF, 0x4FFFE, 0x4FFFF, 0x5FFFE, 0x5FFFF, 0x6FFFE, 0x6FFFF, 0x7FFFE,
                0x7FFFF, 0x8FFFE, 0x8FFFF, 0x9FFFE, 0x9FFFF, 0xAFFFE, 0xAFFFF, 0xBFFFE, 0xBFFFF, 0xCFFFE, 0xCFFFF, 0xDFFFE,
                0xDFFFF, 0xEFFFE, 0xEFFFF, 0xFFFFE, 0xFFFFF, 0x10FFFE, 0x10FFFF);
        NON_CHARACTERS_SET = Collections.unmodifiableSet(codePointsSet);

        codePointsSet = new HashSet<>();
        Collections.addAll(codePointsSet, 0x0009, 0x000A, 0x000C, 0x000D, 0x0020);
        ASCII_WHITESPACES_SET = Collections.unmodifiableSet(codePointsSet);

        codePointsSet = new HashSet<>();
        for (Integer i = 0x0000; i <= 0x001F; i++) {
            codePointsSet.add(i);
        }
        C0_CONTROL_SET = Collections.unmodifiableSet(codePointsSet);

        codePointsSet = new HashSet<>();
        for (Integer i = 0x007F; i <= 0x009F; i++) {
            codePointsSet.add(i);
        }
        codePointsSet.addAll(C0_CONTROL_SET);
        CONTROL_SET = Collections.unmodifiableSet(codePointsSet);

        codePointsSet = new HashSet<>(CONTROL_SET);
        codePointsSet.removeAll(ASCII_WHITESPACES_SET);
        codePointsSet.remove(0x0000);
        CONTROL_WO_WHITESPACES_SET = Collections.unmodifiableSet(codePointsSet);

        Set<Character> charactersSet = new HashSet<>();
        for (int i = 0x0030; i <= 0x0039; i++) {
            charactersSet.add((char) i);
        }
        ASCII_DIGIT = Collections.unmodifiableSet(charactersSet);

        charactersSet = new HashSet<>();
        for (int i = 0x0041; i <= 0x005A; i++) {
            charactersSet.add((char) i);
        }
        ASCII_UPPER_ALPHA = Collections.unmodifiableSet(charactersSet);

        charactersSet = new HashSet<>();
        for (int i = 0x0061; i <= 0x007A; i++) {
            charactersSet.add((char) i);
        }
        ASCII_LOWER_ALPHA = Collections.unmodifiableSet(charactersSet);

        charactersSet = new HashSet<>(ASCII_UPPER_ALPHA);
        charactersSet.addAll(ASCII_LOWER_ALPHA);
        ASCII_ALPHA = Collections.unmodifiableSet(charactersSet);

        charactersSet = new HashSet<>(ASCII_DIGIT);
        charactersSet.addAll(ASCII_ALPHA);
        ASCII_ALPHANUMERIC = Collections.unmodifiableSet(charactersSet);
    }

}
