package main.kotlin.fleet.eval

import main.kotlin.fleet.lexer.Token
import main.kotlin.fleet.parser.*

class Evaluator(global: Environment) {

    private var env: Environment = global
    fun evaluate(stmt: Stmt?) : EvalResult {
        try {
            when (stmt) {
                is Stmt.Program -> {
                    return if (stmt.root != null) evaluate(stmt.root) else EvalResult.Continue
                }

                is Stmt.StoryboardDecl -> {
                    return if (stmt.body != null) evaluate(stmt.body) else EvalResult.Continue
                }

                is Stmt.Block -> {
                    val previous = env
                    env = Environment(previous)    // new inner scope
                    try {
                        val firstResult = stmt.first?.let { evaluate(it) } ?: EvalResult.Continue
                        if (firstResult is EvalResult.ReturnValue) return firstResult

                        val nextResult = stmt.next?.let { evaluate(it) } ?: EvalResult.Continue
                        return nextResult
                    } finally {
                        env = previous             // restore outer scope
                    }
                }

                is Stmt.ActionStmt -> {
                    val result = evaluateExpr(stmt.action)
//                    printResult(result ?: "")
                    // THIS is where we stop the program and bubble up the value
                    return EvalResult.Continue
                }

                is Stmt.PresentStmt -> {
                    val result = evaluateExpr(stmt.value)
                    printResult(result ?: "")
                    // THIS is where we stop the program and bubble up the value
                    return EvalResult.Continue
                }

                is Stmt.AssignStmt -> {
                    val value = evaluateExpr(stmt.value)
                    env.assign(stmt.target.token.lexeme, value)  // use assign, not define
//                    printResult(Helpers.valueWithName(stmt.target.token.lexeme, value))
                    return EvalResult.Continue
                }

                is Stmt.IfStmt -> {
                    val cond = evaluateExpr(stmt.condition)
                    val branch = if (Helpers.isTruthy(cond)) stmt.thenBranch else stmt.elseBranch
                    return if (branch != null) evaluate(branch) else EvalResult.Continue
                }

                is Stmt.SceneStmt -> {
                    val times = stmt.count.token.literal as? Double ?: 0.0
                    repeat(times.toInt()) {
                        val res = stmt.body?.let { evaluate(it) }
                        if (res is EvalResult.ReturnValue) {
                            return res
                        }
                    }
                    return EvalResult.Continue
                }
                is Stmt.ActorDecl -> {
                    // ActorDecl doesn't "run" but you could store it in env if desired
                    val actorName = stmt.name.token.lexeme
                    val actorRole = stmt.role.token.lexeme
                    val typeName = stmt.datatype.lowercase()
                    env.define(actorName, 0)
                    return EvalResult.Continue

                }
                null -> return EvalResult.Continue

                else -> throw RuntimeError(null, "Unknown statement type")
            }
        } catch (e: RuntimeError) {
            val line = (stmt as? Stmt.AssignStmt)?.target?.token?.line ?: 1
            println("[line $line] Runtime error: ${e.message}")
            return EvalResult.Continue
        } catch (e: Exception) {
            println("Unexpected error: ${e.message}")
            return EvalResult.Continue
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

                            left is String || right is String ||
                            left is Char || right is Char -> Helpers.valueToString(left) + Helpers.valueToString(right)
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

        fun valueToString(value: Any?): String {
            return when (value) {
                null -> "nil"
                is Double ->
                    if (value % 1.0 == 0.0) value.toInt().toString()
                    else value.toString()
                is Char -> value.toString()
                else -> value.toString()
            }
        }
    }
}