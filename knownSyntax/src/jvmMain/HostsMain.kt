import org.parserkt.*

object Hosts: AbstractHosts() {
  @JvmStatic fun main(vararg args: String) {
    val hs = hosts.read(CharInput.STDIN)
    println(hs)
  }
}
