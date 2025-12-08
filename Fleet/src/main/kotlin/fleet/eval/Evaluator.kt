package main.kotlin.fleet.eval

import main.kotlin.fleet.lexer.Token
import main.kotlin.fleet.parser.*

class Evaluator(global: Environment) {
    private var env: Environment = global
    private var insideAction = false
    private var insideIfBranch = false
    private var insideScene = false

    fun evaluate(stmt: Stmt?) : EvalResult {
        try {
            when (stmt) {
                is Stmt.Program -> {
                    return if (stmt.root != null) evaluate(stmt.root) else EvalResult.Continue
                }

                is Stmt.ProgramList -> {
                    // First, define all storyboards in the environment
                    for (statement in stmt.statements) {
                        if (statement is Stmt.StoryboardDecl) {
                            env.define(statement.name.token.lexeme, statement)
                        }
                    }

                    // Then automatically run Main if it exists
                    if (stmt.statements.any { it is Stmt.StoryboardDecl && it.name.token.lexeme == "Main" }) {
                        val main = env.get("Main") as Stmt.StoryboardDecl
                        val previous = env
                        env = Environment(previous)

                        try {
                            main.body?.let { evaluate(it) }
                        } finally {
                            env = previous
                        }
                    }

                    return EvalResult.Continue
                }

                is Stmt.StoryboardDecl -> {
                    env.define(stmt.name.token.lexeme, stmt)
                    return EvalResult.Continue
                }

                is Stmt.Block -> {
                    val previous = env
//                    if (!insideScene) env = Environment(previous)

                    try {
                        var block: Stmt.Block? = stmt
                        while (block != null) {
                            block.first?.let { evaluate(it) }
                            block = block.next as? Stmt.Block
                        }
                    } finally {
                        if (!insideScene) env = previous
                    }

                    return EvalResult.Continue
                }

                is Stmt.ActionStmt -> {
                    val prev = insideAction
                    insideAction = true
                    try {
                        if (stmt.expr != null) {
                            evaluateExpr(stmt.expr)
                            return EvalResult.Continue
                        }

                        if (stmt.body != null) {
                            return evaluate(stmt.body)
                        }

                        return EvalResult.Continue
                    } finally {
                        insideAction = prev
                    }
                }

                is Stmt.PresentStmt -> {
                    val result = evaluateExpr(stmt.value)
                    printResult(result ?: "")
                    return EvalResult.Continue
                }

                is Stmt.AssignStmt -> {
                    val value = evaluateExpr(stmt.value)
                    env.assign(stmt.target.token.lexeme, value)
                    return EvalResult.Continue
                }

                is Stmt.IfStmt -> {
                    if (!insideAction) {
                        throw RuntimeError(null, "If-statements may only appear inside Action blocks.")
                    }

                    val cond = evaluateExpr(stmt.condition)
                    val prevInsideIf = insideIfBranch
                    insideIfBranch = true
                    try {
                        return if (Helpers.isTruthy(cond)) {
                            stmt.thenBranch?.let { evaluate(it) } ?: EvalResult.Continue
                        } else {
                            stmt.elseBranch?.let { evaluate(it) } ?: EvalResult.Continue
                        }
                    } finally {
                        insideIfBranch = prevInsideIf
                    }
                }

                is Stmt.SceneStmt -> {
                    val timesExpr = stmt.countExpr  // <-- countExpr should be Expr type
                    val timesValue = evaluateExpr(timesExpr)
                    val times = when(timesValue) {
                        is Double -> timesValue.toInt()
                        is Int -> timesValue
                        else -> throw RuntimeError(null, "Scene count must be a number")
                    }

                    val prevInsideScene = insideScene
                    insideScene = true
                    try {
                        repeat(times) { iteration ->
                            evaluate(stmt.body)
                        }
                    } finally {
                        insideScene = prevInsideScene
                    }

                    return EvalResult.Continue
                }

                is Stmt.ActorDecl -> {
                    val actorName = stmt.name.token.lexeme
                    env.define(actorName, 0)
                    return EvalResult.Continue
                }

                is Stmt.RollStmt -> {
                    val storyboardName = stmt.name.token.lexeme
                    val storyboard = env.get(storyboardName) as? Stmt.StoryboardDecl
                        ?: throw RuntimeError(stmt.name.token, "Storyboard '$storyboardName' not found.")

                    val previousEnv = env
                    env = Environment(previousEnv)

                    try {
                        // Bind the single argument from the map
                        val paramNames = mutableListOf<String>()
                        var paramNode = storyboard.params
                        while (paramNode is ParamList.Param) {
                            paramNames.add(paramNode.name.token.lexeme)
                            paramNode = paramNode.next ?: ParamList.Empty
                        }
                        val argValues = stmt.args?.values?.map { evaluateExpr(it) } ?: emptyList()
                        for (i in paramNames.indices) {
                            env.define(paramNames[i], argValues.getOrNull(i))
                        }

                        return storyboard.body?.let { evaluate(it) } ?: EvalResult.Continue
                    } finally {
                        env = previousEnv
                    }
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

                    return when (expr.operator.token.lexeme) {
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
                        else -> throw RuntimeError(expr.operator.token, "Unknown binary operator.")
                    }
                }
                null -> null
                else -> throw RuntimeError(null, "Unknown expression type")
            }
        } catch (e: RuntimeError) {
            println("Runtime error: ${e.message}")
            null
        }
    }

    private fun printResult(value: Any?) {
        if (value == null) println("nil")
        else if (value is Double && value % 1.0 == 0.0) println(value.toInt())
        else println(value)
    }

    fun bindParameters(params: ParamList){
        var node = params
        while(node is ParamList.Param){
            val name = node.name.token.lexeme
            env.define(name, null)
            node = node.next ?: ParamList.Empty
        }
    }

    object Helpers {
        fun isTruthy(value: Any?) = when(value) {
            null -> false
            is Boolean -> value
            else -> true
        }
        fun checkNumberOperands(operator: Token, left: Any?, right: Any?): Pair<Double, Double> {
            if (left is Double && right is Double) return Pair(left, right)
            throw RuntimeError(operator, "Operands must be numbers.")
        }
        fun valueToString(value: Any?) = when(value) {
            null -> "nil"
            is Double -> if (value % 1.0 == 0.0) value.toInt().toString() else value.toString()
            else -> value.toString()
        }
    }
}

