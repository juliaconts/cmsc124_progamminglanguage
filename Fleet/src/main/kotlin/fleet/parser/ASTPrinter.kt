package main.kotlin.fleet.parser

class ASTPrinter {
    fun printStmt(stmt: Stmt?) {
        println(print(stmt))
    }

    private fun print(stmt: Stmt?): String = when (stmt) {
        is Stmt.Program -> print(stmt.root)
        is Stmt.StoryboardDecl -> "(${stmt.name.token.lexeme} ${stmt.body?.let { print(it) }})"
        is Stmt.ActorDecl -> "(${stmt.name.token.lexeme} ${stmt.role.token.lexeme} ${stmt.datatype})"
        is Stmt.AssignStmt -> "(${stmt.target.token.lexeme} ${printExpr(stmt.value)})"
        is Stmt.ActionStmt -> {
            when {
                stmt.expr != null -> "(Action ${printExpr(stmt.expr)})"
                stmt.body != null -> "(Action ${print(stmt.body)})"
                else -> "(Action)"
            }
        }
        is Stmt.PresentStmt -> "(${printExpr(stmt.value)})"
        is Stmt.SceneStmt -> {
            val countStr = printExpr(stmt.countExpr)             // print the expression
            val bodyStr = stmt.body?.let { print(it) } ?: ""    // print the body recursively
            "Scene($countStr) { $bodyStr }"
        }
        is Stmt.IfStmt -> {
            val elsePart = stmt.elseBranch?.let { " else ${print(it)}" } ?: ""
            "(if ${printExpr(stmt.condition)} ${print(stmt.thenBranch)}$elsePart)"
        }
        is Stmt.Block -> listOfNotNull(stmt.first, stmt.next).joinToString(" ") { print(it) }
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
