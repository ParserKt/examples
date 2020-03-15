import org.parserkt.*
import org.parserkt.util.*
import org.parserkt.pat.*
import org.parserkt.pat.complex.*
import org.parserkt.pat.ext.LexicalBasics

abstract class AbstractSimpleArithmetic {
sealed class Expr {
  data class Op(val id: String, val l: Expr, val r: Expr): Expr()
    { override fun toString() = "$l $id $r" }
  data class TermInt(override val v: Int): Expr(), ConvertAs.Box<Int>
    { override fun toString() = "$v" }
}

lateinit var expr: Pattern<Char, Expr>

val number = Repeat(asInt(), LexicalBasics.digitFor('0'..'9')) typed { Expr.TermInt(it) }
val parened = SurroundBy(LexicalBasics.clamly(parens), Deferred{expr})
val factor: Pattern<Char, Expr> = Decide(number, parened).discardFirst()

val ops = KeywordPattern<InfixOp<Expr>>().apply {
  mergeOps(0, "+", "-")
  mergeOps(1, "*", "/")
}.also { expr = InfixPattern(factor, it) }

private fun KeywordPattern<InfixOp<Expr>>.mergeOps(prec: Int, vararg ops: String) {
  ops.forEach { register(it infixl prec join { l, r -> Expr.Op(it, l, r) }) }
}

}
