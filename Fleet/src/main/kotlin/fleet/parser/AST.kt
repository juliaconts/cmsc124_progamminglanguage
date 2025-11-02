package main.kotlin.fleet.parser

import main.kotlin.fleet.lexer.Token

interface Expr {
    data class Assign(val name: Token, val value: Expr) : Expr
    data class Binary(val left: Expr, val operator: Token, val right: Expr) : Expr
    data class Logical(val left: Expr, val operator: Token, val right: Expr) : Expr
    data class Unary(val operator: Token, val right: Expr) : Expr
    data class Literal(val value: Any?) : Expr
    data class Grouping(val expression: Expr) : Expr
    data class Variable(val name: Token) : Expr
    data class Call(val callee: Expr, val paren: Token, val arguments: List<Expr>) : Expr
}

interface Stmt {
    data class Expression(val expression: Expr) : Stmt
    data class VarDecl(val type: Token?, val name: Token, val initializer: Expr) : Stmt
    data class FunDecl(val name: Token, val params: List<Token>, val body: List<Stmt>) : Stmt
    data class ReturnStmt(val keyword: Token, val value: Expr?) : Stmt
    data class IfStmt(val condition: Expr, val thenBranch: Stmt, val elseBranch: Stmt?) : Stmt
    data class WhileStmt(val condition: Expr, val body: Stmt) : Stmt
    data class Block(val statements: List<Stmt>) : Stmt
}