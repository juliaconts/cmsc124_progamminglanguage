package main.kotlin.fleet.eval

import main.kotlin.fleet.lexer.Token

class RuntimeError(val token: Token?, message: String) : RuntimeException(message)