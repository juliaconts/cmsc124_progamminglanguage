package main.kotlin.fleet.lexer

object KeywordFactory {
    // *maps keywords to token type
    val keywords =  mapOf(
        "True" to TokenType.TRUE,
        "False" to TokenType.FALSE,
        "and" to TokenType.AND,
        "or" to TokenType.OR,
        "not" to TokenType.NOT,
        "null" to TokenType.NULL,

        // arithmetic keywords
        "add" to TokenType.ADD,
        "sub" to TokenType.MINUS,
        "mul" to TokenType.MUL,
        "div" to TokenType.DIV,

        //datatype keywords
        "int" to TokenType.INT,
        "float" to TokenType.FLOAT,
        "char" to TokenType.CHAR,
        "bool" to TokenType.BOOL,
        "String" to TokenType.STRING,

        //storyboard keywords
        "storyboard" to TokenType.STORYBOARD,
        "cut" to TokenType.END,
        "Actor" to TokenType.VAR,
        "Role" to TokenType.ROLE,
        "Assign" to TokenType.ASSIGN,
        "Action" to TokenType.ACTION,
        "to" to TokenType.TO,
        "takes" to TokenType.TIMES,
        "Present" to TokenType.PRINT,

        "Return" to TokenType.RETURN,
        "if" to TokenType.IF,
        "else" to TokenType.ELSE,
        "Scene" to TokenType.LOOP,
        "Roll" to TokenType.CALL
    )
}