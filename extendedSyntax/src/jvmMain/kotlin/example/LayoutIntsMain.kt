package example
import AbstractLayoutInts
import org.parserkt.CharInput
import org.parserkt.STDIN
import org.parserkt.pat.Pattern

object LayoutInts: AbstractLayoutInts() {
  @JvmStatic fun main(vararg args: String) {
    if (args.firstOrNull()?.let { it == "ints" } ?: true) mainOf(ints)
      else mainOf(slashSepValues)
  }

  private fun <T> mainOf(pat: Pattern<Char, T>) {
    val result = pat.read(CharInput.STDIN)
    println(result)
    pat.show(::print, result); println()
  }
}
