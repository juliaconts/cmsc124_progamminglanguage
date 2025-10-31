package example.lexicalscanner.scanner.utils

data class KeywordFactory(val keyword: String) {
    // *maps keywords to token type
    val keywords =  mapOf(
        "var" to TokenType.VAR,
        "def" to TokenType.DEF,
        "return" to TokenType.RETURN,
        "int" to TokenType.INT,
        "float" to TokenType.FLOAT,
        "char" to TokenType.CHAR,
        "if" to TokenType.IF,
        "else" to TokenType.ELSE,
        "while" to TokenType.WHILE,
        "for" to TokenType.FOR
    )
}