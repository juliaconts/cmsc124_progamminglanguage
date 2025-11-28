package main.kotlin.fleet.parser

import main.kotlin.fleet.lexer.*
import main.kotlin.fleet.lexer.TokenType.*

class ParseError(message: String) : RuntimeException(message)

class Parser(val tokens: List<Token>) {
    private var current = 0

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
        val nameToken = consume(IDENTIFIER, "Expected storyboard name (identifier).")
        if (!nameToken.lexeme.first().isUpperCase()) {
            throw error(nameToken, "Storyboard identifier must begin with an uppercase letter.")
        }

        consume(TokenType.LEFT_BRACE, "Expect '{' after storyboard name.")
//        val body = parseBlock()

        val body = parseBlockAfterLeftBrace("Expect '}' after storyboard block.")
        consume(END, "Expect 'cut' at end of storyboard.")
        return Stmt.StoryboardDecl(TokenNode(nameToken), body)
    }


    private fun parseBlockAfterLeftBrace (closeMessage: String): Stmt.Block {
        // CASE 1: empty block → "{}"
        if (check(RIGHT_BRACE)) {
            consume(RIGHT_BRACE, closeMessage)
            return Stmt.Block(null, null)
        }

        // CASE 2: parse the first statement inside the block
        val firstStmt: Stmt? = try {
            parseStatement()
        } catch (e: ParseError) {
            synchronize()
            null
        }

        // If the very next token is '}', this block has only one statement:
        //
        // {
        //   firstStmt
        // }
        if (check(RIGHT_BRACE) || isAtEnd()) {
            consume(RIGHT_BRACE, closeMessage)
            return Stmt.Block(firstStmt, null)
        }

        // CASE 3: there is at least one more statement before '}'
        // Recursively parse "the rest of the block"
        //
        // {
        //   firstStmt
        //   ...restOfBlock...
        // }
        val restOfBlock = parseBlockAfterLeftBrace(closeMessage)

        return Stmt.Block(firstStmt, restOfBlock)
    }

    private fun parseStatement(): Stmt {
        return when {
            match(VAR) -> { consume(EQUALS, "Expect '::' after 'Actor'."); actorStatement() }
            match(ASSIGN) -> { consume(EQUALS, "Expect '::' after 'Assign'."); assignStatement() }
            match(ACTION) -> { consume(EQUALS, "Expect '::' after 'Action'."); Stmt.ActionStmt(parseExpression()) }
            match(PRINT) -> { consume(EQUALS, "Expect '::' after 'Present'."); presentStatement() }
            match(LOOP) -> { consume(EQUALS, "Expect '::' after 'Scene'."); sceneStatement() }
            match(IF) -> ifStatement()
            match(TokenType.LEFT_BRACE) -> { parseBlockAfterLeftBrace("Expect '}' after block.")}
            else -> throw error(peek(), "Unexpected statement.")
        }
    }

    private fun actorStatement(): Stmt {
        val nameToken = consume(IDENTIFIER, "Expect actor name after 'Actor ::'.")
        consume(ROLE, "Expect 'Role' after actor name.")
        consume(EQUALS, "Expect '::' after 'Role'.")
        val datatypeToken = advance().takeIf { it.type in listOf(INT, FLOAT, CHAR, BOOL, STRING) }
            ?: throw error(peek(), "Expect datatype after 'Role ::'.")
        return Stmt.ActorDecl(
            name = TokenNode(nameToken),
            role = TokenNode(nameToken), // optionally use same as variable name
            datatype = datatypeToken.lexeme
        )
    }

    private fun assignStatement(): Stmt {
        val expr = parseExpression()
        consume(TO, "Expect 'to' after expression.")
        val target = consume(IDENTIFIER, "Expect target variable after 'to'.")
        return Stmt.AssignStmt(TokenNode(target), expr)
    }

    private fun presentStatement(): Stmt {
        val expr = parseExpression()
        return Stmt.PresentStmt(expr)
    }

    private fun sceneStatement(): Stmt {
        val count = consume(NUMBER, "Expect scene count.")
        consume(TIMES, "Expect 'takes' after number.")
        val body = parseStatement()
        return Stmt.SceneStmt(TokenNode(count), body)
    }

    private fun ifStatement(): Stmt {
        consume(LEFT_PAR, "Expect '(' after 'if'.")
        val condition = parseExpression()
        consume(RIGHT_PAR, "Expect ')' after if condition.")

        val thenBranch = parseStatement()
        val elseBranch = if (match(ELSE)) if (match(IF)) ifStatement() else parseStatement() else null

        return Stmt.IfStmt(condition, thenBranch, elseBranch)
    }

    // ===== Expressions =====
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

        throw error(peek(), "Expect expression.")
    }

    // ===== Token Helpers =====
    private fun match(vararg types: TokenType): Boolean {
        for (type in types) {
            if (check(type)) {
                advance()
                return true
            }
        }
        return false
    }

    private fun consume(type: TokenType, message: String): Token {
        if (check(type)) return advance()
        throw error(peek(), message)
    }

    private fun check(type: TokenType): Boolean {
        if (isAtEnd()) return false
        return peek().type == type
    }

    private fun advance(): Token {
        if (!isAtEnd()) current++
        return previous()
    }

    private fun isAtEnd(): Boolean = peek().type == EOF

    private fun peek(): Token = tokens[current]
    private fun previous(): Token = tokens[current - 1]

    private fun error(token: Token, message: String): ParseError {
        if (token.type == EOF) println("[line ${token.line}] Error at end: $message")
        else println("[line ${token.line}] Error at '${token.lexeme}': $message")
        return ParseError(message)
    }

    private fun synchronize() {
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
