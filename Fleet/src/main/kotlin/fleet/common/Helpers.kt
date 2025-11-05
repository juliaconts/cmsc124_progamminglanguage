package main.kotlin.fleet.common

import main.kotlin.fleet.eval.RuntimeError
import main.kotlin.fleet.lexer.*
import main.kotlin.fleet.lexer.TokenType.*
import main.kotlin.fleet.parser.*

// Scanner helper functions
fun Scanner.peek(): Char = if (endOfLine()) '\u0000' else source[current] // *peeks at current character
fun Scanner.peekNext(): Char = if (current + 1 >= source.length) '\u0000' else source[current + 1] // *peeks at next character
fun Scanner.endOfLine(): Boolean = current >= source.length
fun Scanner.next(): Char = source[current++] // *reads through text
fun Scanner.addToken(type: TokenType, literal: Any? = null) { // *adds token to mutable list of tokens
    val text = source.substring(start, current)
    readTokens.add(Token(type, text, literal, line))
}
fun Scanner.match(expected: Char): Boolean { // *used in possible multiple character tokens, checks if character after token changes token type
    if (endOfLine()) return false
    if (source[current] != expected) return false
    current++
    return true
}

// Parser helper functions
fun Parser.match(vararg types: TokenType): Boolean {
    for (type in types) {
        if (check(type)) {
            advance()
            return true
        }
    }
    return false
}

fun Parser.consume(type: TokenType, message: String): Token {
    if (check(type)) return advance()
    throw error(peek(), message)
}

fun Parser.check(type: TokenType): Boolean {
    if (isAtEnd()) return false
    return peek().type == type
}

fun Parser.checkNext(type: TokenType): Boolean {
    if (current + 1 >= tokens.size) return false
    return tokens[current + 1].type == type
}

fun Parser.advance(): Token {
    if (!isAtEnd()) current++
    return previous()
}

fun Parser.isAtEnd(): Boolean = peek().type == EOF

fun Parser.peek(): Token = tokens[current]

fun Parser.previous(): Token = tokens[current - 1]

fun Parser.error(token: Token, message: String): ParseError {
    if (token.type == EOF) {
        println("[line ${token.line}] Error at end: $message")
    } else {
        println("[line ${token.line}] Error at '${token.lexeme}': $message")
    }
    return ParseError(message)
}

fun Parser.synchronize() {
    advance()
    while (!isAtEnd()) {
        if (previous().type == SEMICOLON) return
        when (peek().type) {
            DEF, IF, LOOP, RETURN -> return
            else -> {}
        }
        advance()
    }
}

//Evaluator helper functions
object Helpers {
    fun isTruthy(value: Any?): Boolean = when (value) {
        null -> false
        is Boolean -> value
        else -> true
    }

    fun isEqual(a: Any?, b: Any?): Boolean {
        if (a == null && b == null) return true
        if (a == null) return false
        return a == b
    }

    fun checkNumberOperands(operator: Token, left: Any?, right: Any?): Pair<Double, Double> {
        if (left is Double && right is Double) return Pair(left, right)
        throw RuntimeError(operator, "Operands must be numbers.")
    }

    fun checkNumberOperand(operator: Token, operand: Any?): Double {
        if (operand is Double) return operand
        throw RuntimeError(operator, "Operand must be a number.")
    }

    fun valueWithName(name: String, value: Any?): String {
        return when (value) {
            null -> "$name = nil"
            is Double -> if (value % 1.0 == 0.0) "$name = ${value.toInt()}" else "$name = $value"
            else -> "$name = $value"
        }
    }
}
