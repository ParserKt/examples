import org.parserkt.*
import org.parserkt.pat.*
import org.parserkt.pat.complex.*
import org.parserkt.pat.ext.LexicalBasics
import org.parserkt.pat.ext.LayoutPattern
import org.parserkt.util.*

object LayoutInts: LexicalBasics() {
val item = numInt
val tail = Seq(::CharTuple, item('-'),item('>')).toStringPat()
val layout = Convert(Repeat(asString(), item(' ')).Many() prefix item('\n'), { it.length }, { "".padStart(it) })
val p = LayoutPattern(item, tail, layout)

val slash = item('/')
val comment = Seq(::StringTuple, elementIn('#', ';').toStringPat(), *anyChar until newlineChar)
val field = Repeat(asString(), !slash).clamWhile(!slash, "") {"empty field"}
val line = JoinBy(slash, field).mergeConstantJoin()
val record = Decide(comment, line).mergeFirst { if (it is StringTuple) 0 else 1 }
val slashSepValues = JoinBy(newlineChar, record)
}
