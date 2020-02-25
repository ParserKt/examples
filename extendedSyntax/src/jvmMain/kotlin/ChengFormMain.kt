import org.parserkt.*
import org.parserkt.pat.*

object ChengForm: AbstractChengForm() {
  @JvmStatic fun main(vararg args: String) {
    val reverse = args.firstOrNull()?.equals("reverse") ?: false
    val noReverse = args.firstOrNull()?.equals("noReverse") ?: false
    print(
      if (reverse) 橙式.show(CharInput.STDIN.readText().split(全角空格))
      else 橙式.read(CharInput.STDIN.withState(ExpectClose()))?.let { kws ->
        if (noReverse) kws.joinToString("", transform = { it.drop(1) }) else kws.joinToString(全角空格)
      })
  }
}
