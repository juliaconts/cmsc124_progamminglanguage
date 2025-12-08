package main.kotlin.fleet.eval

sealed class EvalResult {
    object Continue : EvalResult()
    data class ReturnValue(val value: Any?) : EvalResult()
}