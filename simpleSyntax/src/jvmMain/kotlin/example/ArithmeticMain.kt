package example
import AbstractArithmetic

import org.parserkt.*
import org.parserkt.pat.*

object Arithmetic: AbstractArithmetic() {
  @JvmStatic fun main(vararg args: String) {
    val (e, input) = CharInput.STDIN.withState(ExpectClose()).addErrorList()
    val parsed = expr.read(input)
    println(parsed)
    println("= ${parsed?.eval()}")
    println(e)
  }
}
