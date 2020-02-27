import org.parserkt.*
import org.parserkt.pat.*
import org.parserkt.pat.complex.*
import org.parserkt.pat.ext.LexicalBasics
import org.parserkt.pat.ext.LayoutPattern
import org.parserkt.pat.ext.Deep
import org.parserkt.util.*

abstract class AbstractLayoutInts: LexicalBasics() {
val item = numInt
val tail = Seq(::CharTuple, item('-'),item('>')).toStringPat()
val layout = Convert(Repeat(asString(), item(' ')).Many() prefix item('\n'), { it.length }, { "".padStart(it) })
val ints = object: LayoutPattern<Char, Int, String>(item, tail, layout) {
  override fun show(s: Output<Char>, value: Deep<Int, String>?) { value?.visitBy(ShowVisitor(s, 2)) }
}

val NL = newlineChar.toConstant('\n')
val slash = item('/')
val comment = prefix1(elementIn('#', ';'), stringFor(!NL))
val field = Repeat(asString(), !(slash or NL))
val line = JoinBy(slash, field).Rescue { s, dl -> s.clamWhile(!slash, "", "empty field in ${dl.first}") }.mergeConstantJoin()
val record = Decide(comment, line).mergeFirst { if (it is String) 0 else 1 } suffix NL
val slashSepValues = Repeat(asList(), record).Many()
}
