package main.kotlin.fleet.eval

import main.kotlin.fleet.eval.RuntimeError
class Environment(private val enclosing: Environment? = null) {
    private val values: MutableMap<String, Any?> = mutableMapOf()

    // Toggle this to true for verbose environment tracing
    var debug = false

    fun define(name: String, value: Any?) {
        if (debug) println("ENV.define: '$name' = $value (in ${this.hashCode()})")
        values[name] = value
    }

    fun assign(name: String, value: Any?) {
        if (values.containsKey(name)) {
            if (debug) println("ENV.assign: setting existing '$name' = $value (in ${this.hashCode()})")
            values[name] = value
            return
        }

        if (enclosing != null) {
            if (debug) println("ENV.assign: '$name' not found here, delegating to enclosing (this=${this.hashCode()}, enclosing=${enclosing.hashCode()})")
            enclosing.assign(name, value)
            return
        }

        // Not found anywhere -> throw meaningful runtime error (matches other code's expectation)
        if (debug) println("ENV.assign: ERROR undefined variable '$name' (no enclosing)")
        throw RuntimeError(null, "Undefined variable '$name'.")
    }

    fun get(name: String): Any? {
        if (values.containsKey(name)) {
            val v = values[name]
            if (debug) println("ENV.get: found '$name' = $v (in ${this.hashCode()})")
            return v
        }

        if (enclosing != null) {
            if (debug) println("ENV.get: '$name' not in this, checking enclosing (this=${this.hashCode()}, enclosing=${enclosing.hashCode()})")
            return enclosing.get(name)
        }

        if (debug) println("ENV.get: ERROR undefined variable '$name' (no enclosing)")
        throw RuntimeError(null, "Undefined variable '$name'.")
    }

    // Optional helper for tests/debugging: dump all variables visible in this environment chain
    fun dumpAll() {
        println("=== ENV DUMP (this=${this.hashCode()}) ===")
        values.forEach { (k, v) -> println("  $k = $v") }
        if (enclosing != null) {
            println("--- enclosing ---")
            enclosing.dumpAll()
        }
    }
}