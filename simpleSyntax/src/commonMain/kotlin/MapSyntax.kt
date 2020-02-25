import org.parserkt.*
import org.parserkt.pat.*
import org.parserkt.pat.complex.*
import org.parserkt.pat.ext.LexicalBasics
import org.parserkt.util.*

abstract class AbstractMapSyntax: LexicalBasics() {
lateinit var list: Pattern<Char, List<Any>>
val comma = item(',').tokenizePunction()
val colon = item(':').tokenizePunction()

val string = SurroundBy(clamly(quotes), stringFor(!item('\'')))
val value: Pattern<Char, Any> = Decide(numInt, Deferred{list}).mergeFirst { if (it is List<*>) 1 else 0 }
init { list = SurroundBy(clamly(squares), comma seprated value) }

val kvPart = Seq(::AnyTuple, string, colon, value)
val kv = Convert(kvPart, { it[0] to it[2] }, { anyTupleOf(it.first, ':', it.second) })
val mapPart = SurroundBy(clamly(braces), comma seprated kv)
val map = Convert(mapPart, { it.toMap() }, { it.toList() })

}
