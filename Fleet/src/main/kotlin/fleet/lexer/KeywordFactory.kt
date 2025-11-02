package main.kotlin.fleet.lexer

object KeywordFactory {
    // *maps keywords to token type
    val keywords =  mapOf(
        "TT" to TokenType.TRUE,
        "FF" to TokenType.FALSE,
        "&&" to TokenType.AND,
        "||" to TokenType.OR,
        "nl" to TokenType.NULL,
        "-d" to TokenType.DEF,
        "-r" to TokenType.RETURN,
        "#i" to TokenType.INT,
        "#f" to TokenType.FLOAT,
        "@c" to TokenType.CHAR,
        "%b" to TokenType.BOOL,
        "^i" to TokenType.IF,
        "^e" to TokenType.ELSE,
        "^w" to TokenType.WHILE,
        "^f" to TokenType.FOR
    )
}