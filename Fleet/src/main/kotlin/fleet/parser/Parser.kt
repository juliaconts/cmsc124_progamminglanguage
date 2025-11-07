package main.kotlin.fleet.parser

import main.kotlin.fleet.lexer.*
import main.kotlin.fleet.lexer.TokenType.*

class ParseError(message: String) : RuntimeException(message)

class Parser(val tokens: List<Token>) {
    var current = 0

    fun parse(): Stmt.Program {
        val root = try {
            parseStoryboard()
        } catch (e: ParseError) {
            synchronize()
            null
        }
        return Stmt.Program(root)
    }

    private fun parseStoryboard(): Stmt? {
        consume(STORYBOARD, "Expect 'storyboard' at start.")
        val body = parseBlock()
        consume(END, "Expect 'cut' at end of storyboard.")
        return body
    }

    private fun parseBlock(): Stmt.Block? {
        consume(LEFT_BRACE, "Expect '{' after storyboard.")
        var first: Stmt? = null
        var currentStmt: Stmt.Block? = null

        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            val stmt = try {
                parseStatement()
            } catch (e: ParseError) {
                synchronize()
                null
            }
            if (stmt != null) {
                if (first == null) {
                    first = stmt
                    currentStmt = Stmt.Block(first, null)
                } else {
                    currentStmt = Stmt.Block(currentStmt, stmt)
                }
            }
        }

        consume(RIGHT_BRACE, "Expect '}' after storyboard block.")
        return currentStmt
    }

    private fun parseStatement(): Stmt {
        return when {
            match(VAR) -> { consume(EQUALS, "Expect '::' after 'Actor'."); actorStatement() }
            match(ASSIGN) -> { consume(EQUALS, "Expect '::' after 'Assign'."); assignStatement() }
            match(ACTION) -> { consume(EQUALS, "Expect '::' after 'Action'."); Stmt.ActionStmt(parseExpression()) }
            match(PRINT) -> { consume(EQUALS, "Expect '::' after 'Present'."); presentStatement() }
            match(LOOP) -> { consume(EQUALS, "Expect '::' after 'Scene'."); sceneStatement() }
            match(IF) -> ifStatement()
            else -> throw error(peek(), "Unexpected statement.")
        }
    }

    private fun actorStatement(): Stmt {
        val name = consume(IDENTIFIER, "Expect actor name.")
        if (!match(TokenType.DATATYPE)) error(previous(), "Actor must be followed by a role.")
        val role = consume(IDENTIFIER, "Expect role name after 'Role ::'.")
        return Stmt.ActorDecl(TokenNode(name), TokenNode(role))
    }

    private fun assignStatement(): Stmt {
        if (match(ACTION)) {
            val expr = parseExpression()
            consume(TO, "Expect 'to' after expression.")
            val target = consume(IDENTIFIER, "Expect target variable after 'to'.")
            return Stmt.AssignStmt(TokenNode(target), expr)
        } else {
            val expr = parseExpression()
            consume(TO, "Expect 'to' after expression.")
            val target = consume(IDENTIFIER, "Expect target variable after 'to'.")
            return Stmt.AssignStmt(TokenNode(target), expr)
        }
    }

    private fun presentStatement(): Stmt {
        val expr = parseExpression()
        return Stmt.PresentStmt(expr)
    }

    private fun sceneStatement(): Stmt {
        val count = consume(NUMBER, "Expect scene count.")
        consume(TokenType.TIMES, "Expect 'takes' after number.")
        val body = parseStatement()
        return Stmt.SceneStmt(TokenNode(count), body)
    }

    private fun ifStatement(): Stmt {
        consume(LEFT_PAR, "Expect '(' after 'if'.")
        val condition = parseExpression()
        consume(RIGHT_PAR, "Expect ')' after if condition.")

        val thenBranch = parseStatement()
        var elseBranch: Stmt? = null

        if (match(ELSE)) {
            elseBranch = if (match(IF)) ifStatement() else parseStatement()
        }

        return Stmt.IfStmt(condition, thenBranch, elseBranch)
    }

    // ===== EXPRESSIONS =====

    private fun parseExpression(): Expr = equality()

    private fun equality(): Expr {
        var expr = comparison()
        while (match(EQUAL_EQUAL, NOT_EQUAL)) {
            val operator = previous()
            val right = comparison()
            expr = Expr.Binary(expr, TokenNode(operator), right)
        }
        return expr
    }

    private fun comparison(): Expr {
        var expr = term()
        while (match(GREATER, GREATER_EQUAL, LESSER, LESSER_EQUAL)) {
            val operator = previous()
            val right = term()
            expr = Expr.Binary(expr, TokenNode(operator), right)
        }
        return expr
    }

    private fun term(): Expr {
        var expr = factor()
        while (match(ADD, MINUS)) {
            val operator = previous()
            val right = factor()
            expr = Expr.Binary(expr, TokenNode(operator), right)
        }
        return expr
    }

    private fun factor(): Expr {
        var expr = unary()
        while (match(MUL, DIV)) {
            val operator = previous()
            val right = unary()
            expr = Expr.Binary(expr, TokenNode(operator), right)
        }
        return expr
    }

    private fun unary(): Expr {
        if (match(NEGATIVE, MINUS, NOT)) {
            val operator = previous()
            val right = unary()
            return Expr.Unary(TokenNode(operator), right)
        }
        return primary()
    }

    private fun primary(): Expr {
        if (match(FALSE)) return Expr.Literal(false)
        if (match(TRUE)) return Expr.Literal(true)
        if (match(STRING)) return Expr.Literal(previous().literal)
        if (match(NUMBER)) return Expr.Literal(previous().literal)
        if (match(IDENTIFIER)) return Expr.Variable(TokenNode(previous()))

        if (match(LEFT_PAR)) {
            val expr = parseExpression()
            if (!match(RIGHT_PAR)) error(peek(), "Missing closing parenthesis.")
            return Expr.Grouping(expr)
        }

        throw error(peek(), "Expect expression after 'Action ::'.")
    }

    fun match(vararg types: TokenType): Boolean {
        for (type in types) {
            if (check(type)) {
                advance()
                return true
            }
        }
        return false
    }

    fun consume(type: TokenType, message: String): Token {
        if (check(type)) return advance()
        throw error(peek(), message)
    }

    fun check(type: TokenType): Boolean {
        if (isAtEnd()) return false
        return peek().type == type
    }

    fun checkNext(type: TokenType): Boolean {
        if (current + 1 >= tokens.size) return false
        return tokens[current + 1].type == type
    }

    fun advance(): Token {
        if (!isAtEnd()) current++
        return previous()
    }

    fun isAtEnd(): Boolean = peek().type == EOF

    fun peek(): Token = tokens[current]

    fun previous(): Token = tokens[current - 1]

    fun error(token: Token, message: String): ParseError {
        if (token.type == EOF) {
            println("[line ${token.line}] Error at end: $message")
        } else {
            println("[line ${token.line}] Error at '${token.lexeme}': $message")
        }
        return ParseError(message)
    }

    fun synchronize() {
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
}