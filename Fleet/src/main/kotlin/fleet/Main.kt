package main.kotlin.fleet

import main.kotlin.fleet.lexer.Scanner
import main.kotlin.fleet.parser.Parser
import main.kotlin.fleet.parser.ASTPrinter

fun main() {
    while (true) {
        print("> ")
        val line = readlnOrNull() ?: break
        if (line.trim() == "exit") break

        val scanner = Scanner(line)
        val tokens = scanner.scanInput()
        val parser = Parser(tokens)
        val statements = parser.parse()

        val printer = ASTPrinter()
        printer.printProgram(statements)
    }
}