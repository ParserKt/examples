import kotlin.math.abs

import org.parserkt.*
import org.parserkt.util.*
import org.parserkt.pat.*
import org.parserkt.pat.complex.*
import org.parserkt.pat.ext.*

abstract class AbstractHanCalc: LexicalBasics() {
val wsLn = stringFor(elementIn(' ', '\t')).toConstant("")

val hexPart = Repeat(asInt(16), hex)
val binPart = Repeat(asInt(2), bin)

val hanSign = Convert(elementIn('+', '-', '正', '负').toDefault('+'), { it in setOf('-', '负') }, { if(it) '-' else '+' })
// 0x / 0b / 123
val zeroNotation = Decide(
  hexPart prefix item('x'),
  binPart prefix item('b'),
  StickyEnd(item('0') or EOF, 0) { if (octal.test(peek)) error("no octal notations");  -1 }
).discardFirst()

fun dotFraction(i: Int) = Repeat(asDouble(i.toLong()), numInt) prefix item('.')
val numPart = Contextual(digit) {
  if (it == 0) zeroNotation
  else Piped(Repeat(asInt(10, it), digit).Many()) { it?.let(::dotFraction)?.let { it.read(this)?.toInt() } ?: it }
}.discardFirst()

val int = Convert(Contextual(hanSign) { sign ->
  Piped(Decide(numPart, hanNum).discardFirst()) { if (sign && it!=notParsed) -it else it }
}, { it.second as Number }, { Tuple2(it.toInt()<0, abs(it.toInt())) })

lateinit var expr: Pattern<Char, Number>
val atomParen = SurroundBy(clamly(parens), Deferred {expr})
val atomInt = SurroundBy(wsLn to wsLn, int)
val atom = Decide(atomInt, atomParen).discardFirst()

internal fun fn(join: InfixJoin<Int>): InfixJoin<Number> = { a, b -> join(a.toInt(), b.toInt()) }
val ops = KeywordPattern<InfixOp<Number>>().apply {
  listOf("*", "乘").forEach { register(it infixl 0 join fn(Int::times)) }
  listOf("/", "除以").forEach { register(it infixl 0 join fn(Int::div)) }
  register("除" infixl 0 join flip(fn(Int::div)))
  val realDiv = { a: Number, b: Number-> (a.toDouble() / b.toDouble()) as Number }
  register("分之" infixl 0 join realDiv)
  register("分" infixl 0 join flip(realDiv))
  listOf("+", "加") .forEach { register(it infixl 1 join fn(Int::plus)) }
  listOf("-", "减") .forEach { register(it infixl 1 join fn(Int::minus)) }
}

init {
  val duoLine = int prefix item('\n')
  expr = InfixPattern(atom, ops).Rescue { s, base, op1 ->
    print("|")
    duoLine.read(s) ?: notParsed.also { s.error("expecting rhs for $base $op1") }
  }
}

} //AbstractHanCalc
