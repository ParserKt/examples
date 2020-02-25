import org.parserkt.*

object JSONParser: AbstractJSONParser() {
  @JvmStatic fun main(vararg args: String) {
    val parsed = json.read(CharInput.STDIN)
    println(parsed); println(json.show(parsed))
  }
}
