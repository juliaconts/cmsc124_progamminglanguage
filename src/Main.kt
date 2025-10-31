package example.lexicalscanner

import example.lexicalscanner.scanner.utils.*


fun main() {
    while (true) {
        print("> ")
        val line = readlnOrNull() ?: break
        if (line.trim() == "exit") break

        val scanner = Scanner(line)
        val tokens = scanner.scanInput()

        for (token in tokens) {
            println("Token(type = ${token.type}, lexeme = ${token.lexeme}, literal = ${token.literal}, line = ${token.line})")
        }
    }
}