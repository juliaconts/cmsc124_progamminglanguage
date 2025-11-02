package main.kotlin.fleet.lexer

enum class TokenType {
    // grouping tokens
    LEFT_PAR, RIGHT_PAR, LEFT_BRACE, RIGHT_BRACE, COMMA, DOT, SEMICOLON,

    // assignment and comparison tokens
    EQUALS, NOT, EQUAL_EQUAL, NOT_EQUAL, LESSER, LESSER_EQUAL, GREATER, GREATER_EQUAL,

    // arithmetic tokens
    PLUS, MINUS, STAR, SLASH,

    // logical operators
    AND, OR, TRUE, FALSE,

    // literals
    IDENTIFIER, STRING, NUMBER, BOOL, NULL,

    // keywords
    DEF, RETURN, INT, FLOAT, CHAR, IF, ELSE, WHILE, FOR,

    EOF
}