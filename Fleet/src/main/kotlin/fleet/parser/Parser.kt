package main.kotlin.fleet.parser

import main.kotlin.fleet.common.*
import main.kotlin.fleet.lexer.*
import main.kotlin.fleet.lexer.TokenType.*

class ParseError(message: String) : RuntimeException(message)

class Parser(val tokens: List<Token>) {
    var current = 0

    fun parse(): List<Stmt> {
        val statements = mutableListOf<Stmt>()
        while (!isAtEnd()) {
            try {
                declarations()?.let { statements.add(it) }
            } catch (err: ParseError) {
                synchronize()
            }
        }
        return statements
    }

    // Top-level: declarations
    fun declarations(): Stmt? {
        return when {
            match(DEF) -> funDeclaration()
            else -> statement()
        }
    }

    fun funDeclaration(): Stmt {
        val name = consume(IDENTIFIER, "Expect function name after 'DEF'.")
        consume(TokenType.LEFT_PAR, "Expect '(' after function name.")
        val parameters = mutableListOf<Token>()
        if (!check(TokenType.RIGHT_PAR)) {
            do {
                if (parameters.size >= 255) error(peek(), "Can't have more than 255 parameters.")
                parameters.add(consume(IDENTIFIER, "Expect parameter name."))
            } while (match(COMMA))
        }
        consume(TokenType.RIGHT_PAR, "Expect ')' after parameters.")
        val body = blockStatements()
        return Stmt.FunDecl(name, parameters, body)
    }

    // Statements
    fun statement(): Stmt {
        return when {
            match(RETURN) -> returnStatement()
            match(IF) -> ifStatement()
            match(WHILE) -> whileStatement()
            match(LEFT_BRACE) -> Stmt.Block(blockStatements())
            else -> expressionStatement()
        }
    }

    fun returnStatement(): Stmt {
        val keyword = previous()
        var value: Expr? = null
        if (!check(SEMICOLON)) {
            value = expression()
        }
        consume(SEMICOLON, "Expect ';' after return value.")
        return Stmt.ReturnStmt(keyword, value)
    }

    fun ifStatement(): Stmt {
        consume(LEFT_PAR, "Expect '(' after 'IF'.")
        val condition = expression()
        consume(RIGHT_PAR, "Expect ')' after if condition.")
        val thenBranch = statement()
        var elseBranch: Stmt? = null
        if (match(ELSE)) {
            elseBranch = statement()
        }
        return Stmt.IfStmt(condition, thenBranch, elseBranch)
    }

    fun whileStatement(): Stmt {
        consume(LEFT_PAR, "Expect '(' after 'WHILE'.")
        val condition = expression()
        consume(RIGHT_PAR, "Expect ')' after condition.")
        val body = statement()
        return Stmt.WhileStmt(condition, body)
    }

    fun blockStatements(): List<Stmt> {
        val statements = mutableListOf<Stmt>()
        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            declarations()?.let { statements.add(it) }
        }
        consume(RIGHT_BRACE, "Expect '}' after block.")
        return statements
    }

    fun expressionStatement(): Stmt {
        // support varDecl: IDENTIFIER '=' expr ';'
        if (match(INT, FLOAT, CHAR, BOOL)) {
            val type = previous()
            val name = consume(IDENTIFIER, "Expect variable name after type.")
            consume(EQUALS, "Expect '=' after variable name.")
            val initializer = expression()
            consume(SEMICOLON, "Expect ';' after variable declaration.")
            return Stmt.VarDecl(type, name, initializer)
        }

        val expr = expression()
        consume(SEMICOLON, "Expect ';' after expression.")
        return Stmt.Expression(expr)
    }

    // Expressions: assignment -> logical -> equality -> comparison -> term -> factor -> unary -> primary
    fun expression(): Expr = assignment()

    fun assignment(): Expr {
        val expr = logical()
        if (match(EQUALS)) {
            val equals = previous()
            val value = assignment()
            if (expr is Expr.Variable) {
                val name = expr.name
                return Expr.Assign(name, value)
            }
            error(equals, "Invalid assignment target.")
        }
        return expr
    }

    fun logical(): Expr {
        var expr = equality()
        while (match(AND, OR)) {
            val operator = previous()
            val right = equality()
            expr = Expr.Logical(expr, operator, right)
        }
        return expr
    }

    fun equality(): Expr {
        var expr = comparison()
        while (match(EQUAL_EQUAL, NOT_EQUAL)) {
            val operator = previous()
            val right = comparison()
            expr = Expr.Binary(expr, operator, right)
        }
        return expr
    }

    fun comparison(): Expr {
        var expr = term()
        while (match(GREATER, GREATER_EQUAL, LESSER, LESSER_EQUAL)) {
            val operator = previous()
            val right = term()
            expr = Expr.Binary(expr, operator, right)
        }
        return expr
    }

    fun term(): Expr {
        var expr = factor()
        while (match(PLUS, MINUS)) {
            val operator = previous()
            val right = factor()
            expr = Expr.Binary(expr, operator, right)
        }
        return expr
    }

    fun factor(): Expr {
        var expr = unary()
        while (match(STAR, SLASH)) {
            val operator = previous()
            val right = unary()
            expr = Expr.Binary(expr, operator, right)
        }
        return expr
    }

    fun unary(): Expr {
        if (match(NOT, MINUS)) {
            val operator = previous()
            val right = unary()
            return Expr.Unary(operator, right)
        }
        return call()
    }

    fun call(): Expr {
        var expr = primary()
        while (true) {
            if (match(LEFT_PAR)) {
                expr = finishCall(expr)
            } else {
                break
            }
        }
        return expr
    }

    fun finishCall(callee: Expr): Expr {
        val arguments = mutableListOf<Expr>()
        if (!check(RIGHT_PAR)) {
            do {
                if (arguments.size >= 255) error(peek(), "Can't have more than 255 arguments.")
                arguments.add(expression())
            } while (match(COMMA))
        }
        val paren = consume(RIGHT_PAR, "Expect ')' after arguments.")
        return Expr.Call(callee, paren, arguments)
    }

    fun primary(): Expr {
        if (match(FALSE)) return Expr.Literal(false)
        if (match(TRUE)) return Expr.Literal(true)
        if (match(NULL)) return Expr.Literal(null)
        if (match(NUMBER)) return Expr.Literal(previous().literal)
        if (match(STRING)) return Expr.Literal(previous().literal)
        if (match(IDENTIFIER)) return Expr.Variable(previous())

        if (match(LEFT_PAR)) {
            val expr = expression()
            consume(RIGHT_PAR, "Expect ')' after expression.")
            return Expr.Grouping(expr)
        }

        throw error(peek(), "Expect expression.")
    }
}