package example
import AbstractSimpleArithmetic

import org.parserkt.*

object SimpleArithmetic: AbstractSimpleArithmetic() {
  @JvmStatic fun main(vararg args: String) {
    println(expr.read(CharInput.STDIN))
  }
}

