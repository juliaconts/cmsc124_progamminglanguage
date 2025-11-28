package main.kotlin.fleet.eval

import main.kotlin.fleet.eval.RuntimeError

data class ActorValue(val value: Any?, val datatype: String)

class Environment (private val enclosing: Environment? = null) {
    private val values = mutableMapOf<String, Any?>()

    fun define(name: String, value: Any?) {
        values[name] = value
    }

    fun get(name: String): Any? {
        if (name in values) return values[name]
        if (enclosing != null) return enclosing.get(name)
        throw RuntimeError(null, "Undefined variable '$name'.")
    }

    fun assign(name: String, value: Any?) {
        if (name in values){
            values[name] = value
            return
        }
        if (enclosing != null){
            enclosing.assign(name,value)
            return
        }
        throw RuntimeError(null, "Variable '$name' is not declared.")
    }

}