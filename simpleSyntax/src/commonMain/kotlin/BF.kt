import org.parserkt.*
import org.parserkt.util.*
import org.parserkt.pat.*
import org.parserkt.pat.complex.*

abstract class AbstractBF {
sealed class BF {
  data class Op(val id: Char): BF() { override fun toString() = "Op($id)" }
  data class Blk(val body: List<BF>): BF() { override fun toString() = "Blk[${body.joinToString(" ")}]" }
}
val control = elementIn('>', '<', '+', '-', '.', ',') named "control"
val controlBF = Convert(control, { BF.Op(it) }, { it.id })
lateinit var program: Pattern<Char, List<BF>>
val block = SurroundBy(item('[') to item(']').clam {"] !!!"}, Deferred {program})
val blockBF = Convert(block, { BF.Blk(it) }, { it.body })

val ws = Repeat(asString(), !(control or elementIn('[',']','\n')) named "ws").Many()
init {
  val part = JoinBy(ws, Decide(controlBF, blockBF).mergeFirst { if (it is BF.Op) 0 else 1 }).mergeConstantJoin("")
  program = Convert(Seq(::AnyTuple, ws, part, ws), { it.getAs<List<BF>>(1) }, { anyTupleOf("",it,"") })
}

}
