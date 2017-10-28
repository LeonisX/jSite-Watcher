package md.leonis.parser.domain;

public enum State {

    DATA,
    RCDATA,
    RAWTEXT,
    SCRIPT_DATA,
    PLAINTEXT,
    TAG_OPEN,
    END_TAG_OPEN,
    TAG_NAME,
    RCDATA_LESS_THAN_SIGN,
    RCDATA_END_TAG_OPEN,
    RCDATA_END_TAG_NAME,
    RAWTEXT_LESS_THAN_SIGN,
    RAWTEXT_END_TAG_OPEN,
    RAWTEXT_END_TAG_NAME,
    SCRIPT_LESS_THAN_SIGN,
    SCRIPT_END_TAG_OPEN,
    SCRIPT_END_TAG_NAME,

    /*12.2.5.18 Script data escape start state
    12.2.5.19 Script data escape start dash state
    12.2.5.20 Script data escaped state
    12.2.5.21 Script data escaped dash state
    12.2.5.22 Script data escaped dash dash state
    12.2.5.23 Script data escaped less-than sign state
    12.2.5.24 Script data escaped end tag open state
    12.2.5.25 Script data escaped end tag name state
    12.2.5.26 Script data double escape start state
    12.2.5.27 Script data double escaped state
    12.2.5.28 Script data double escaped dash state
    12.2.5.29 Script data double escaped dash dash state
    12.2.5.30 Script data double escaped less-than sign state
    12.2.5.31 Script data double escape end state*/

    BEFORE_ATTRIBUTE_NAME,
    ATTRIBUTE_NAME,
    AFTER_ATTRIBUTE_NAME,
    BEFORE_ATTRIBUTE_VALUE,
    ATTRIBUTE_VALUE_DOUBLE_QUOTED,
    ATTRIBUTE_VALUE_SINGLE_QUOTED,
    ATTRIBUTE_VALUE_UNQUOTED,
    AFTER_ATTRIBUTE_VALUE_QUOTED,
    SELF_CLOSING_START_TAG,
    BOGUS_COMMENT,
    MARKUP_DECLARATION_OPEN,

    /*12.2.5.43 comment start state
    12.2.5.44 comment start dash state
    12.2.5.45 comment state
    12.2.5.46 comment less-than sign state
    12.2.5.47 comment less-than sign bang state
    12.2.5.48 comment less-than sign bang dash state
    12.2.5.49 comment less-than sign bang dash dash state
    12.2.5.50 comment end dash state
    12.2.5.51 comment end state
    12.2.5.52 comment end bang state
    12.2.5.53 DOCTYPE state
    12.2.5.54 Before DOCTYPE name state
    12.2.5.55 DOCTYPE name state
    12.2.5.56 After DOCTYPE name state
    12.2.5.57 After DOCTYPE public keyword state
    12.2.5.58 Before DOCTYPE public identifier state
    12.2.5.59 DOCTYPE public identifier (double-quoted) state
    12.2.5.60 DOCTYPE public identifier (single-quoted) state
    12.2.5.61 After DOCTYPE public identifier state
    12.2.5.62 Between DOCTYPE public and system identifiers state
    12.2.5.63 After DOCTYPE system keyword state
    12.2.5.64 Before DOCTYPE system identifier state
    12.2.5.65 DOCTYPE system identifier (double-quoted) state
    12.2.5.66 DOCTYPE system identifier (single-quoted) state
    12.2.5.67 After DOCTYPE system identifier state
    12.2.5.68 Bogus DOCTYPE state
    12.2.5.69 CDATA section state
    12.2.5.70 CDATA section bracket state
    12.2.5.71 CDATA section end state*/

    CHARACTER_REFERENCE,
    NAMED_CHARACTER_REFERENCE,
    AMBIGUOUS_AMPERSAND,
    NUMERIC_CHARACTER_REFERENCE,
    HEXADEMICAL_CHARACTER_REFERENCE_START,
    DECIMAL_CHARACTER_REFERENCE_START,
    HEXADEMICAL_CHARACTER_REFERENCE,
    DECIMAL_CHARACTER_REFERENCE,
    NUMERIC_CHARACTER_REFERENCE_END

}