package example
import AbstractHanCalc
import hanNum

import kotlin.math.abs

import org.parserkt.*
import org.parserkt.pat.*
import org.parserkt.pat.complex.JoinBy
import org.parserkt.pat.complex.mergeConstantJoin

object HanCalc: AbstractHanCalc() {
  @JvmStatic fun main(vararg args: String) {
    fun ps1() = print("> ")
    ps1()
    val input = CharInput.STDIN
    val line = Decide(expr, StickyEnd(EOF, 233)).discardFirst()
    val repl = JoinBy(item('\n'), line).OnItem { println("= $it"); ps1() }.mergeConstantJoin()
    val calcLogs = input.catchError { repl.read(input) }
    calcLogs?.mapNotNull { abs(it.toInt()).let { hanNum.show(it) } }?.let(::println)
  }
}
