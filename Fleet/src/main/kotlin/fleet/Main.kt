package main.kotlin.fleet

import main.kotlin.fleet.lexer.Scanner
import main.kotlin.fleet.parser.Parser
import main.kotlin.fleet.parser.Stmt
import main.kotlin.fleet.eval.Environment
import main.kotlin.fleet.eval.Evaluator
import main.kotlin.fleet.eval.RuntimeError
import java.io.File

fun main(args: Array<String>) {
    if (args.isNotEmpty()) {
        // SCRIPT MODE: read from a text file (e.g., java -jar ProgLang.jar text.txt)
        val path = args[0]
        val source = File(path).readText()
        runScript(source)
    } else {
        // REPL MODE: interactive input
        runRepl()
    }
}

/**
 * SCRIPT MODE
 * Runs a complete source string once (no prompting, no '>').
 */
fun runScript(source: String) {
    val env = Environment()
    val evaluator = Evaluator(env)

    try {
        val scanner = Scanner(source)
        val tokens = scanner.scanInput()

//        val parser = Parser(tokens)
        val program: Stmt = Parser(tokens).parse()

        // Just run it once; Present handles printing.
        evaluator.evaluate(program)

    } catch (e: RuntimeError) {
        println("Runtime error: ${e.message}")
    } catch (e: Exception) {
        println("Unexpected error: ${e.message}")
    }
}

/**
 * REPL MODE
 * Multiline REPL: user types until a line ending with 'cut', then it parses & runs.
 */
fun runRepl() {
    val env = Environment()
    val evaluator = Evaluator(env)
    val buffer = StringBuilder()

    while (true) {
        print("> ")
        val line = readlnOrNull() ?: break
        val trimmed = line.trim()

        if (trimmed == "exit") break
        if (trimmed.isEmpty()) continue

        // Collect every line into buffer
        buffer.appendLine(line)

        // Only parse when we see 'cut' (end of storyboard)
        if (!trimmed.endsWith("cut")) {
            continue
        }

        // Now we have a complete program in buffer
        val source = buffer.toString()
        buffer.clear()

        try {
            val scanner = Scanner(source)
            val tokens = scanner.scanInput()

//            val parser = Parser(tokens)
            val program: Stmt = Parser(tokens).parse()

            // Run once; Present prints everything.
            evaluator.evaluate(program)

        } catch (e: RuntimeError) {
            println("Runtime error: ${e.message}")
        } catch (e: Exception) {
            println("Unexpected error: ${e.message}")
        }
    }
}