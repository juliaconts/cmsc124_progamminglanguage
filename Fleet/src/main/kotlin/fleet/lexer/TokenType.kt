package main.kotlin.fleet.lexer

enum class TokenType {
    // grouping tokens
    LEFT_PAR, RIGHT_PAR, LEFT_BRACE, RIGHT_BRACE, COMMA, DOT, SEMICOLON,

    // assignment and comparison tokens
    EQUALS, NOT, EQUAL_EQUAL, NOT_EQUAL, LESSER, LESSER_EQUAL, GREATER, GREATER_EQUAL, COLON,

    // arithmetic tokens
    ADD, MINUS, MUL, DIV, NEGATIVE,

    // logical operators
    AND, OR, TRUE, FALSE,

    // literals
    VAR, IDENTIFIER, STRING, NUMBER, BOOL, NULL,

    // keywords
    DEF, RETURN, INT, FLOAT, CHAR, IF, ELSE, LOOP,

    // special keywords
    STORYBOARD, ASSIGN, ACTION, CALL, TO, PRINT, END, DATATYPE, TIMES,

    EOF
}