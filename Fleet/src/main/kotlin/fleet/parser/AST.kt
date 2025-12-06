package main.kotlin.fleet.parser

//import main.kotlin.fleet.lexer.Token
import main.kotlin.fleet.lexer.TokenNode

interface ParamList {
    data class Param(val name: TokenNode, val next: ParamList?) : ParamList
    object Empty : ParamList
}

interface Expr {
    data class Literal(val value: Any?) : Expr
    data class Variable(val name: TokenNode) : Expr
    data class Unary(val operator: TokenNode, val right: Expr) : Expr
    data class Binary(val left: Expr, val operator: TokenNode, val right: Expr) : Expr
    data class Grouping(val expression: Expr) : Expr
}


interface Stmt {
    data class Program(val root: Stmt?) : Stmt
    data class StoryboardDecl(val name: TokenNode, val params: ParamList, val body: Stmt?) : Stmt
    data class Block(val first: Stmt?, val next: Stmt?) : Stmt
    data class ActorDecl(val name: TokenNode, val role: TokenNode, val datatype: String) : Stmt
    data class AssignStmt(val target: TokenNode, val value: Expr) : Stmt
    data class ActionStmt(val expr: Expr?, val body: Stmt?) : Stmt
    data class PresentStmt(val value: Expr) : Stmt
    data class SceneStmt(val countExpr: Expr, val body: Stmt?) : Stmt
    data class IfStmt(val condition: Expr, val thenBranch: Stmt, val elseBranch: Stmt?) : Stmt
    data class RollStmt(val name: TokenNode, val args: Map<String, Expr>? = null) : Stmt
}