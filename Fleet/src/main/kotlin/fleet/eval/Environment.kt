package main.kotlin.fleet.eval

class Environment {
    private val values = mutableMapOf<String, Any?>()

    fun define(name: String, value: Any?) {
        values[name] = value
    }

    fun get(name: String): Any? {
        if (values.containsKey(name)) return values[name]
        throw RuntimeError(null, "Undefined variable '$name'.")
    }
}