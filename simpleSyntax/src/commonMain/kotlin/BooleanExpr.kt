import org.parserkt.*
import org.parserkt.util.*
import org.parserkt.pat.*
import org.parserkt.pat.complex.*
import org.parserkt.pat.ext.*

import BooleanExprToken.Textual.*
import BooleanExprToken.White
import BooleanExprToken.EOF

sealed class BooleanExprToken {
  sealed class Textual(override val v: String): BooleanExprToken(), ConvertAs.Box<String> {
    class Name(name: String): Textual(name)
    class Op(id: String): Textual(id)
    class Prefix(id: String): Textual(id)
    class Paren(v: String): Textual(v)
    override fun toString() = v
  }
  class White(override val v: Cnt): BooleanExprToken(), ConvertAs.Box<Cnt> {
    override fun toString() = "".padStart(v, ' ')
  }
  object EOF: BooleanExprToken() {
    override fun toString() = ""
  }
}

object BooleanExprTokenizer: LexicalBasics() {
  val wordChar = elementIn('A'..'Z', 'a'..'z', '0'..'9') or item('_')
  val id = Repeat(asString(), wordChar) typed ::Name
  val op = MapPattern(mapOf(
    '&' to Op("&"),
    '|' to Op("|")
  ))
  val prefix = MapPattern(mapOf(
    '!' to Prefix("!")
  ))
  val ignoreWs = Repeat(asCount(), white) typed ::White
  val parens = elementIn('(', ')').toStringPat() typed ::Paren
  val lexical = Decide<Char, BooleanExprToken>(id, op, prefix, ignoreWs, parens).discardFirst()
}

//>>> BooleanExprTokenFeed(inputOf("abcd&das!|()")).toList()
//res3: kotlin.collections.List<BooleanExprToken> = [abcd, &, das, !, |, (, )]

class BooleanExprTokenFeed(feed: CharInput): LexerFeed<BooleanExprToken>(feed) {
  override fun tokenizer() = BooleanExprTokenizer.lexical
  override val eof = BooleanExprToken.EOF
  override fun consume(): BooleanExprToken = super.consume().also {
    while (peek is White) consume()
  }
}

sealed class BooleanExpr {
  data class NameRef(val name: String): BooleanExpr()
  data class Concat(val id: String, val left: BooleanExpr, val right: BooleanExpr): BooleanExpr()
  data class Unary(val id: String, val expr: BooleanExpr): BooleanExpr()
}

object BooleanExprParser {
  lateinit var expr: Pattern<BooleanExprToken, BooleanExpr>
  val name = Convert(term<Name>()) { BooleanExpr.NameRef(it.v) }
  val parened = SurroundBy(term(Paren("(" )) to term(Paren(")")), Deferred{expr})
  val unaryAssoc = Decide(name, parened).discardFirst()
  val unary = Convert(Seq(::AnyTuple, term<Prefix>(), unaryAssoc))
    { BooleanExpr.Unary(it.getAs<Prefix>(0).v, it.getAs<BooleanExpr>(1)) }
  val atom: Pattern<BooleanExprToken, BooleanExpr> = Decide(unary, name, parened).discardFirst()

  val precL = mapOf("&" to 0, "|" to 1)
  val ops = Convert(term<Op>()) {
   val op: InfixJoin<BooleanExpr> = { a, b -> BooleanExpr.Concat(it.v, a, b) }
   it.v infixl precL.getValue(it.v) join op
  }
  init { expr = InfixPattern(atom, ops) }

  fun read(input: CharInput) = expr.read(BooleanExprTokenFeed(input))

  private inline fun <reified T: BooleanExprToken.Textual> term(instance: T): ConstantPattern<BooleanExprToken, T>
    = term<T> { it.v == instance.v }.toConstant(instance)
  private inline fun <reified T: BooleanExprToken> term(crossinline predicate: Predicate<T> = {true})
    = itemTyped<T, BooleanExprToken>(predicate)
}
