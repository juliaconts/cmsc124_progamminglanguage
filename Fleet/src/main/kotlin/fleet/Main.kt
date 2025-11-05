package main.kotlin.fleet

import main.kotlin.fleet.lexer.Scanner
import main.kotlin.fleet.parser.Parser
import main.kotlin.fleet.parser.Stmt
import main.kotlin.fleet.eval.Environment
import main.kotlin.fleet.eval.Evaluator

fun main() {
    val env = Environment()           // shared environment for variable storage
    val evaluator = Evaluator(env)    // your evaluator handles all errors internally

    while (true) {
        print("> ")
        val line = readlnOrNull() ?: break
        if (line.trim() == "exit") break
        if (line.isBlank()) continue

        // Step 1: Scan input
        val scanner = Scanner(line)
        val tokens = scanner.scanInput()

        // Step 2: Parse tokens into AST
        val parser = Parser(tokens)
        val program: Stmt.Program = parser.parse()

        // Step 3: Evaluate AST (errors are handled internally)
        evaluator.evaluate(program)
    }
}

//        for (token in tokens) {
//            println("Token(type = ${token.type}, lexeme = ${token.lexeme}, literal = ${token.literal}, line = ${token.line})")
//        }
// aaaaaaaaaa