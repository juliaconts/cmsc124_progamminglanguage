package main.kotlin.fleet.eval

import main.kotlin.fleet.lexer.Token
import main.kotlin.fleet.parser.*

class Evaluator(private val env: Environment) {

    fun evaluate(stmt: Stmt?) {
        try {
            when (stmt) {
                is Stmt.Program -> evaluate(stmt.root)
                is Stmt.Block -> {
                    stmt.first?.let { evaluate(it) }
                    stmt.next?.let { evaluate(it) }
                }
                is Stmt.ActionStmt -> {
                    val result = evaluateExpr(stmt.action)
                    printResult(result ?: "")
                }
                is Stmt.PresentStmt -> {
                    val result = evaluateExpr(stmt.value)
                    printResult( result ?: "")
                }
                is Stmt.AssignStmt -> {
                    val value = evaluateExpr(stmt.value)
                    env.define(stmt.target.token.lexeme, value)
                    printResult(Helpers.valueWithName(stmt.target.token.lexeme, value))
                }
                is Stmt.IfStmt -> {
                    val cond = evaluateExpr(stmt.condition)
                    if (Helpers.isTruthy(cond)) {
                        evaluate(stmt.thenBranch)
                    } else {
                        stmt.elseBranch?.let { evaluate(it) }
                    }
                }
                is Stmt.SceneStmt -> {
                    val times = stmt.count.token.literal as? Double ?: 0.0
                    repeat(times.toInt()) {
                        stmt.body?.let { evaluate(it) }
                    }
                }
                is Stmt.ActorDecl -> {
                    // ActorDecl doesn't "run" but you could store it in env if desired
                    env.define(stmt.name.token.lexeme, stmt.role.token.lexeme)
                }
                null -> return
                else -> throw RuntimeError(null, "Unknown statement type")
            }
        } catch (e: RuntimeError) {
            val line = (stmt as? Stmt.AssignStmt)?.target?.token?.line ?: 1
            println("[line $line] Runtime error: ${e.message}")
        } catch (e: Exception) {
            println("Unexpected error: ${e.message}")
        }
    }

    private fun evaluateExpr(expr: Expr?): Any? {
        return try {
            when (expr) {
                is Expr.Literal -> expr.value
                is Expr.Variable -> env.get(expr.name.token.lexeme)
                is Expr.Grouping -> evaluateExpr(expr.expression)
                is Expr.Unary -> {
                    val right = evaluateExpr(expr.right)
                    when (expr.operator.token.lexeme) {
                        "-" -> if (right is Double) -right else throw RuntimeError(expr.operator.token, "Operand must be a number.")
                        "!" -> !Helpers.isTruthy(right)
                        else -> null
                    }
                }
                is Expr.Binary -> {
                    val left = evaluateExpr(expr.left)
                    val right = evaluateExpr(expr.right)
                    when (expr.operator.token.lexeme) {
                        "add" -> when {
                            left is Double && right is Double -> left + right
                            left is String && right is String -> left + right
                            else -> throw RuntimeError(expr.operator.token, "Operands must be two numbers or two strings.")
                        }
                        "sub" -> {
                            val (l, r) = Helpers.checkNumberOperands(expr.operator.token, left, right)
                            l - r
                        }
                        "mul" -> {
                            val (l, r) = Helpers.checkNumberOperands(expr.operator.token, left, right)
                            l * r
                        }
                        "div" -> {
                            val (l, r) = Helpers.checkNumberOperands(expr.operator.token, left, right)
                            if (r == 0.0) throw RuntimeError(expr.operator.token, "Division by zero.")
                            l / r
                        }
                        ">" -> {
                            val (l, r) = Helpers.checkNumberOperands(expr.operator.token, left, right)
                            l > r
                        }
                        ">=" -> {
                            val (l, r) = Helpers.checkNumberOperands(expr.operator.token, left, right)
                            l >= r
                        }
                        "<" -> {
                            val (l, r) = Helpers.checkNumberOperands(expr.operator.token, left, right)
                            l < r
                        }
                        "<=" -> {
                            val (l, r) = Helpers.checkNumberOperands(expr.operator.token, left, right)
                            l <= r
                        }
                        "==" -> Helpers.isEqual(left, right)
                        "!=" -> !Helpers.isEqual(left, right)
                        else -> null
                    }
                }
                null -> null
                else -> throw RuntimeError(null, "Unknown expression type")
            }
        } catch (e: RuntimeError) {
            val line = (expr as? Expr.Variable)?.name?.token?.line ?: 1
            println("[line $line] Runtime error: ${e.message}")
            null
        } catch (e: Exception) {
            println("Unexpected error: ${e.message}")
            null
        }
    }

    private fun printResult(value: Any?) {
        if (value == null) {
            println("nil")
        } else if (value is Double && value % 1.0 == 0.0) {
            // Convert double with no fractional part to integer
            println(value.toInt())
        } else {
            println(value)
        }
    }

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
}