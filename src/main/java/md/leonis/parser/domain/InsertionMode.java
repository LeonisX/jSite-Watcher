package md.leonis.parser.domain;

public enum InsertionMode {

    // The insertion mode is a state variable that controls the primary operation of the tree construction stage.

    // Initially, the insertion mode is "initial". It can change to "before html", "before head", "in head",
    // "in head noscript", "after head", "in body", "text", "in table", "in table text", "in caption",
    // "in column group", "in table body", "in row", "in cell", "in select", "in select in table", "in template",
    // "after body", "in frameset", "after frameset", "after after body", and "after after frameset" during the
    // course of the parsing, as described in the tree construction stage. The insertion mode affects how tokens
    // are processed and whether CDATA sections are supported.

    INITIAL,
    BEFORE_HTML,
    BEFORE_HEAD,
    IN_HEAD,
    IN_HEAD_NOSCRIPT,
    AFTER_HEAD,
    IN_BODY,
    TEXT,
    IN_TABLE,
    IN_TABLE_TEXT,
    IN_CAPTION,
    IN_COLUMN_GROUP,
    IN_TABLE_BODY,
    IN_ROW,
    IN_CELL,
    IN_SELECT,
    IN_SELECT_IN_TABLE,
    IN_TEMPLATE,
    AFTER_BODY,
    IN_FRAMESET,
    AFTER_FRAMESET,
    AFTER_AFTER_BODY,
    AFTER_AFTER_FRAMESET

    // Several of these modes, namely "in head", "in body", "in table", and "in select", are special,
    // in that the other modes defer to them at various times. When the algorithm below says that the user agent
    // is to do something "`using the rules for` the m insertion mode", where m is one of these modes,
    // the user agent must use the rules described under the m insertion mode's section, but must leave
    // the insertion mode unchanged unless the rules in m themselves switch the insertion mode to a new value.

    // When the insertion mode is switched to "text" or "in table text", the `original insertion mode` is also set.
    // This is the insertion mode to which the tree construction stage will return.

    // Similarly, to parse nested template elements, a `stack of template insertion modes` is used.
    // It is initially empty. The `current template insertion mode` is the insertion mode that was most recently
    // added to the stack of template insertion modes. The algorithms in the sections below will push
    // insertion modes onto this stack, meaning that the specified insertion mode is to be added to the stack,
    // and pop insertion modes from the stack, which means that the most recently added insertion mode
    // must be removed from the stack.
}
