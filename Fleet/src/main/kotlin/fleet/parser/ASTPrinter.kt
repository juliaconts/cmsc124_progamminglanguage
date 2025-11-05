package main.kotlin.fleet.parser

class ASTPrinter {
    fun printStmt(stmt: Stmt?) {
        println(print(stmt))
    }

    private fun print(stmt: Stmt?): String = when (stmt) {
        is Stmt.Program -> print(stmt.root)
        is Stmt.ActorDecl -> "(${stmt.name.token.lexeme} ${stmt.role.token.lexeme})"
        is Stmt.AssignStmt -> "(${stmt.target.token.lexeme} ${printExpr(stmt.value)})"
        is Stmt.ActionStmt -> printExpr(stmt.action)
        is Stmt.PresentStmt -> "(${printExpr(stmt.value)})"
        is Stmt.SceneStmt -> "(${stmt.count.token.lexeme} ${stmt.body?.let { print(it) }})"
        is Stmt.IfStmt -> "(${printExpr(stmt.condition)} ${print(stmt.thenBranch)}" +
                (stmt.elseBranch?.let { " ${print(it)}" } ?: "") + ")"
        is Stmt.Block -> "${print(stmt.first)} ${print(stmt.next)}"
        else -> ""
    }

    private fun printExpr(expr: Expr?): String = when (expr) {
        is Expr.Literal -> expr.value.toString()
        is Expr.Variable -> expr.name.token.lexeme
        is Expr.Grouping -> printExpr(expr.expression)
        is Expr.Unary -> "(${expr.operator.token.lexeme} ${printExpr(expr.right)})"
        is Expr.Binary -> "(${expr.operator.token.lexeme} ${printExpr(expr.left)} ${printExpr(expr.right)})"
        else -> ""
    }
}
