import org.parserkt.*
import org.parserkt.pat.*

object MapSyntax: AbstractMapSyntax() {
  @JvmStatic fun main(vararg args: String) {
    val (e, input) = CharInput.STDIN.withState(ExpectClose()).addErrorList()
    val parsed = map.read(input)
    println(parsed)
    println(map.show(parsed))
    println(e)
  }
}
