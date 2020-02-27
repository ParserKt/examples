package example
import AbstractBF

import org.parserkt.*
import org.parserkt.pat.*
import org.parserkt.pat.complex.*

object BF: AbstractBF() {
  @JvmStatic fun main(vararg args: String) {
    val repl = JoinBy(item('\n'), Decide(program, StickyEnd(EOF, notParsed)).mergeFirst {0}).OnItem(::println).mergeConstantJoin()
    repl.read(CharInput.STDIN)?.let { println(repl.show(it)) }
  }
}
