package main.kotlin.fleet.parser

class ASTPrinter {

    /** Print all top-level statements in a program. */
    fun printProgram(statements: List<Stmt>) {
        for (stmt in statements) {
            println(formatStmt(stmt))
        }
    }

    /** Format a statement into a readable string. */
    private fun formatStmt(stmt: Stmt): String {
        return when (stmt) {
            is Stmt.Expression -> "(expr ${formatExpr(stmt.expression)})"
            is Stmt.VarDecl -> {
                val type = stmt.type?.lexeme ?: "var"
                "(var $type ${stmt.name.lexeme} = ${formatExpr(stmt.initializer)})"
            }
            is Stmt.FunDecl -> {
                val params = stmt.params.joinToString(" ") { it.lexeme }
                val body = stmt.body.joinToString(" ") { formatStmt(it) }
                "(def ${stmt.name.lexeme} ($params) $body)"
            }
            is Stmt.ReturnStmt -> "(return ${stmt.value?.let { formatExpr(it) } ?: "nil"})"
            is Stmt.IfStmt -> {
                val cond = formatExpr(stmt.condition)
                val then = formatStmt(stmt.thenBranch)
                val els = stmt.elseBranch?.let { " else ${formatStmt(it)}" } ?: ""
                "(if $cond then $then$els)"
            }
            is Stmt.WhileStmt -> "(while ${formatExpr(stmt.condition)} ${formatStmt(stmt.body)})"
            is Stmt.Block -> stmt.statements.joinToString(" ") { formatStmt(it) }.let { "(block $it)" }

            else -> "(unknown-stmt)"
        }
    }

    /** Format an expression into a readable string. */
    private fun formatExpr(expr: Expr): String {
        return when (expr) {
            is Expr.Assign -> "(assign ${expr.name.lexeme} ${formatExpr(expr.value)})"
            is Expr.Binary -> parenthesize(expr.operator.lexeme, expr.left, expr.right)
            is Expr.Logical -> parenthesize(expr.operator.lexeme, expr.left, expr.right)
            is Expr.Unary -> parenthesize(expr.operator.lexeme, expr.right)
            is Expr.Grouping -> parenthesize("group", expr.expression)
            is Expr.Literal -> expr.value?.toString() ?: "null"
            is Expr.Variable -> expr.name.lexeme
            is Expr.Call -> {
                val args = expr.arguments.joinToString(" ") { formatExpr(it) }
                "(call ${formatExpr(expr.callee)} $args)"
            }

            else -> "(unknown-expr)"
        }
    }

    /** Helper to make "(op arg1 arg2)" style expressions. */
    fun parenthesize(name: String, vararg exprs: Expr): String {
        val builder = StringBuilder()
        builder.append("(").append(name)
        for (expr in exprs) {
            builder.append(" ").append(formatExpr(expr))
        }
        builder.append(")")
        return builder.toString()
    }
}
