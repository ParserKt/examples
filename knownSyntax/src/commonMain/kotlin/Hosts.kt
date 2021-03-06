import org.parserkt.*
import org.parserkt.util.*
import org.parserkt.pat.*
import org.parserkt.pat.complex.*
import org.parserkt.pat.ext.*

abstract class AbstractHosts: LexicalBasics() {
//comment: COMMENT;
val nl = Seq(::CharTuple, item('\r').toDefault(), item('\n')).toStringPat()
val nlChar = elementIn('\r', '\n')
//NL: ('\r\n'|'\n\r'|'\r'|'\n') ->skip;
//BLANK: (' '|'\t') ->skip;
override val white = elementIn(' ', '\t')

//HOSTNAME: [A-z0-9.]+;
val hostname = Until(nl, asString(), elementIn('A'..'z', '0'..'9') or elementIn('.', '-'))

//NUM: [0-9] +;
val number = RepeatUn(asInt(), digit) { it.toString().map { it - '0' } }
//IPADDRESS: NUM '.' NUM '.' NUM '.' NUM;
val ipAddress = JoinBy(item('.'), number).mergeConstantJoin()

//item: IPADDRESS HOSTNAME ;
val record = Seq<Char, Any, HostEntry>(::HostEntry, ws,
  ipAddress, ws,
  hostname.clamWhile(!nlChar, "?") {"bad hostname"}, ws, nl)
class HostEntry: AnyTuple(6) {
  var ipAddress by indexAs<List<Int>>(1)
  var hostname by indexAs<String>(3)
}

//COMMENT: '#' .*? '\r'? '\n';
val comment = Seq(::StringTuple, item('#').toStringPat(), Until(nlChar, asString(), anyChar), nl)

//line : item | comment;
val line = Decide(
  Convert(comment, { Line.Comment(it) }, { it.t }),
  Convert(record, { Line.Record(it) }, { it.t }),
  Convert(nl, { Line.Comment(tupleOf(::StringTuple, "#", it)) }, { it.t[1] }),
  StickyEnd(nlChar or EOF, notParsed) { clamWhile(!nlChar, Line.Unknown, "unknown line") }
).mergeFirst { if (it is Line.Comment) 1 else 0 }
//hostfile: line* EOF;
val hosts = Repeat(asList(), line).Many()

sealed class Line {
  data class Record(val t: HostEntry): Line() {
    override fun toString() = "${t.ipAddress} ${t.hostname}"
  }
  data class Comment(val t: StringTuple): Line() {
    override fun toString() = "#${t[1]}"
  }
  object Unknown: Line()
}

}
