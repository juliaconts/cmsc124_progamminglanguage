package main.kotlin.fleet

import main.kotlin.fleet.lexer.Scanner
import main.kotlin.fleet.parser.Parser
import main.kotlin.fleet.parser.ASTPrinter

fun main() {
    val printer = ASTPrinter()

    while (true) {
        print("> ")
        val line = readlnOrNull() ?: break
        if (line.trim() == "exit") break
        if (line.isBlank()) continue

        val scanner = Scanner(line)
        val tokens = scanner.scanInput()

        val parser = Parser(tokens)
        val tree = parser.parse()
        printer.printStmt(tree)
    }
}

//        for (token in tokens) {
//            println("Token(type = ${token.type}, lexeme = ${token.lexeme}, literal = ${token.literal}, line = ${token.line})")
//        }
// aaaaaaaaaa