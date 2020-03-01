# Examples

Example parsers using the ParserKt library :heart:

Let's create a CSV parser from scratch! Create a Gradle project with Kotlin(JS/JVM/Multiplatform) first.

## Dependency config for ParserKt

__Import JitPack.io repository first__:

```groovy
repositories {
  maven { url "https://jitpack.io" }
}
```

ParserKt is a Kotlin-multiplatform library, you can use it in Kotlin JVM/JS project simply:

```groovy
dependencies {
  implementation "com.github.parserkt.parserkt:parserkt-jvm:$pktVersion"
  //^ or parserkt-js for Kotlin/JS

  implemeentation "com.github.parserkt.parserkt:parserkt-ext-jvm:$pktVersion"
  //^ for org.parserkt.pat.ext.*
}
```

### ...for multiplatform projects

Since we are extracting the same part from more platforms, __it's a little more complicated__ for using multiplatform library in multiplatform projects.

```groovy
dependencies {
  commonMainImplementation "com.github.parserkt.parserkt:parserkt-metadata:$ver"
  //^ for Kotlin-multiplatform metadata
  commonMainImplementation "com.github.parserkt.parserkt:parserkt-util-metadata:$ver"
  commonMainImplementation "com.github.parserkt.parserkt:parserkt-ext-metadata:$ver"
  // NOTE: com.github.parserkt.parserkt:parserkt-ext without metadata is also available

  jvmMainImplementation "com.github.parserkt.parserkt:parserkt-jvm:$ver"
  jvmMainImplementation "com.github.parserkt.parserkt:parserkt-util-jvm:$ver"
  jvmMainImplementation "com.github.parserkt.parserkt:parserkt-ext-jvm:$ver"
  //... jsMainImplementation for nearly-the-same package coordinates
}
```

Too much boilerplate code! But you can also add `enableFeaturePreview("GRADLE_METADATA")` to root project's `settings.gradle`, then just add a single dependency:

```kotlin
dependencies {
  commonMainImplementation "com.github.parserkt:parserkt:$pktVersion"
}
```

> NOTE: Gradle metadata is an __experimental feature__, using even future release of Gradle `!= 5.6.2` may fail.<br>
If build configuration acts wrong with single-dependency, use old-style with `forEach` instead.

```kotlin
dependencies {
  ["", "-util", "-ext"].forEach {
    commonMainImplementation "com.github.parserkt.parserkt:parserkt$it-metadata:$pktVersion"
    jvmMainImplementation "com.github.parserkt.parserkt:parserkt$it-jvm:$pktVersion"
    jsMainImplementation "com.github.parserkt.parserkt:parserkt$it-js:$pktVersion"
  }
}
```

## Creating CSV parser

## Creating infix calculator

## Documents

ParserKt is about `Feed<T>` sequence input, and `Pattern<IN, T>` extracting data from input.

It can also perform a process called "rebuild" — re-structure input sequence back from parse result. for this reason, combinators e.g. `Decide` should use `mergeFirst`/`discardFirst` to retrieve `caseNo` <a href="#About_mergeXXX">back</a> from output value.

```kotlin
interface Feed<out T> {
  val peek: T
  fun consume(): T //< throws End
  class End: NoSuchElementException("no more")
}

interface Pattern<IN, T> {
  fun read(s: Feed<IN>): T?
  fun show(s: Output<IN> /*(T) -> Unit*/, value: T?)
}
```

Sequential inputs are divied into subparts by sub-patterns. For matched inputs, `Pattern.read` will return de-structed value, otherwise it will return `null` (aka `notParsed`)

_(some super-types are omitted for brevity)_

> See [org.parserkt.pat](https://github.com/ParserKt/ParserKt/tree/master/src/commonMain/kotlin/org/parserkt/pat), basic model is defined in [PatternModel.kt](https://github.com/ParserKt/ParserKt/blob/master/src/commonMain/kotlin/org/parserkt/pat/PatternModel.kt)

```kotlin
// == Patterns ==
// SURDIES (Seq, Until, Repeat, Decide) (item, elementIn, satisfy, StickyEnd) (always, never)
// CCDP (Convert, Contextual, Deferred, Piped)
// SJIT (SurroundBy, JoinBy) (InfixPattern, TriePattern)

// == Extensions ==
// ArrangeModel: Sized, Slice, Tuple
// FoldModel: Fold
// TextPreety: Preety
// InputLayer: Feed, Input, CharInput

// == With OnItem / Rescue ==
// CharInput.OnItem
// JoinBy.OnItem, JoinBy.Rescue
// InfixPattern.Rescue

// == Special Repeat ==
// Repeat.InBounds(bounds, greedy = true)
// Repeat.Many -- with support for OptionalPattern

// == Error Handling ==
// Input.addErrorList
// clam(messager), clamWhile(pat, defaultValue, messager)
// Pattern.toDefault(defaultValue), ConstantPattern.toDefault()

// == State ==
// AllFeed.withState, AllFeed.stateAs
// alsoDo -- Pattern, SatisfyPattern, SatisfyEqualTo
```

Detailed documents rest will be given by-example :kissing_heart:

```kotlin
// import first if you want to tryout codes
import org.parserkt.*
import org.parserkt.util.*
import org.parserkt.pat.*
import org.parserkt.pat.complex.*
import org.parserkt.pat.ext.*
```

### Atom pattern definition "IES"

> "IES" means item, elementIn, satisfy and StickyEnd [AtomIES.kt](https://github.com/ParserKt/ParserKt/blob/master/src/commonMain/kotlin/org/parserkt/pat/AtomIES.kt)

```kotlin
val letter = elementIn('a'..'z', 'A'..'Z') or item('_')
val digit = elementIn('0'..'9')
val digitNZ = digit and !item('0')

val _3to4 = elementIn(3.0..4.0)
```

```kotlin
letter.test('a') //true
letter.read('_') //_
digitNZ.read("9") //9
digitNZ.read("0") //notParsed
_3to4.read(3.1415926) //3.1415926
```

```kotlin
val white = elementIn(' ', '\t', '\n', '\r')
val dashPrefixed = satisfy<String>("dash prefixed") { it.startsWith("-") }
```

```kotlin
white //(' '|'\t'|'\n'|'\r')
white named "whitespace" //whitespace
dashPrefixed.test("-remove") //true

anyChar //anyChar
EOF //EOF
//^ EOF='\uFFFF', for rules supporting REPL
Seq(::CharTuple, item('a'), StickyEnd(EOF, 'e')).read(CharInput.STDIN)
//^ a<^D><^D> = (a, e)
// where ^=Key Control
```

`StickyEnd` means "value OK to `peek` but throwing `Feed.End` when `consume`", for instance, [disambiguate octal-int-notation](https://github.com/ParserKt/examples/blob/master/hanCalc/src/commonMain/kotlin/Calc.kt#L20).

```kotlin
StickyEnd(pat_may_occur_again_in_peek, result_value_is_end, op_not_end = {notParsed})
```

for detailed designment about sticky end, see [FeedModel.kt](https://github.com/ParserKt/ParserKt/blob/master/src/commonMain/kotlin/org/parserkt/FeedModel.kt#L31)

> If you don't run operation decide only by `peek` (and never `consume`), `StickyEnd` is not a problem

### Combined pattern "SURD"

> "SURD" means Seq, __Until, Repeat__, Decide [CombSURD.kt](https://github.com/ParserKt/ParserKt/blob/master/src/commonMain/kotlin/org/parserkt/pat/CombSURD.kt)

\*Until, Repeat: They are `FoldPattern`, for multiply(repeating) reading, pair them for memorizing

~~absurd~~ memorizing tricks~

For impreative program control flow, we have sequential-execution, branch condition, repeation. For pattern matching we have `Seq`, `Decide`, `Repeat`.

```kotlin
val _2char = Seq(::CharTuple, anyChar, anyChar)
_2char.read("a") //notParsed
_2char.read("ab") //(a, b)

val untilDash = Seq(::StringTuple, *item<Char>()/*=anyChar*/ until item('-'))
untilDash.read("h") //notParsed
untilDash.read("helloworld-") //(helloworld, -)
```

#### Operations on `Tuple`

> See [parserkt-util:ArrangementModel.kt](https://github.com/ParserKt/ParserKt/blob/master/parserkt-util/src/commonMain/kotlin/org/parserkt/util/ArrangeModel.kt) for more operations

```kotlin
class Point: IntTuple(2) {
  var x by index(0)
  var y by index(1)
}
val int = Repeat(asInt(), LexicalBasics.digitFor('0'..'9'))
val point = SurroundBy(parens.toCharPat(), Seq(::Point, int suffix item(','), int))

point.read("(123,43)") //(123, 43) //Point

// Seq will NOT discard constant pattern items
// reason: not suitable for type system
```

```kotlin
// Typed tuples: Int,Long,Float,Double,Char,String are provided
tupleOf(::IntTuple, 1,2,3) //(1, 2, 3)
val (a,b) = tupleOf(::IntTuple, 1,2)

// Dynamic tuples: AnyTuple
val t = anyTupleOf("hello", 1)
t.getAs<Int>(1) //1

class Student: AnyTuple(2) {
  val name by indexAs<String>(0)
  var age by indexAs<Int>(1)
}
Student() //(null, null)
val john = tupleOf(::Student, "john", 17)
john.age++ //17
john //(john, 18)
```

#### `Until` and `Repeat`

Until: zero or more (also known as "many") `item` until `terminate`(could be tested with 1 char) is parsed

> __Remeber__, ParserKt has __no capacity__ for resetting input stream back

> NOTE: Never use `Until(terminate, fold pat)` when `Repeat(fold, !pat)` is sufficient

```kotlin
object Ints: LexicalBasics() {
  val int = numInt
  val note = Until(int, asString(), white)
  val ints = Repeat(asList(), int suffix item(' '))
  //^ not a good approach
}

Ints.ints.read("10 20 30 45 ") //[10, 20, 30, 45]
Ints.note.rebuild(" \t\n233 666 ")?.rawString() //" \t\n"
```

```kotlin
open class StringPart: LexicalBasics() {
  val escapes = mapOf('"' to '"', 'n' to '\n')
  val backslash = item('\\')
  val escape = MapPattern(escapes) prefix backslash

  val char = Decide(!(backslash or item('"')), escape).mergeFirst { if (it in escapes.values) 1 else 0 }
  val string = SurroundBy(clamly(dquotes), Repeat(asString(), char))
}

StringPart().string.read("\"hello\\nworld\"") //"hello\nworld"
```

Inner class `Repeat.InBounds(bounds, greedy = true)` and `Repeat.Many()` are also avaliable. [source](https://github.com/ParserKt/ParserKt/blob/master/src/commonMain/kotlin/org/parserkt/pat/CombSURD.kt#L99)

#### Use `UntilUn`, `RepeatUn`

`Fold` is a non-invertible operation (e.g. reading numbers by shifting), but providing an `unfold` operation back to `Iterable<IN>` will enable rebuilding for such fold-pattern

Default unfold operation for `String` and `List` (actually `Iterable`) are provided

```kotlin
// Improve: use LexicalBasics.digitFor & pat.ext.asInt()
val digit = Convert(elementIn('0'..'9'), { it-'0' }, { '0'+it })
val asInt = JoinFold(0) { this*10 + it }

val int = RepeatUn(asInt, digit) { i -> i.toString().map { it-'0' } }

int.read("2019") //2019 //Int
int.show(2019) //2019 //String
```

### Wrapper pattern "CCDP"

> "CCDP" means Convert, Contextual, Deferred, Piped [WrapperCCDP.kt](https://github.com/ParserKt/ParserKt/blob/master/src/commonMain/kotlin/org/parserkt/pat/WrapperCCDP.kt)

> [pat/ext/MiscHelper.kt](https://github.com/ParserKt/ParserKt/blob/master/parserkt-ext/src/commonMain/kotlin/org/parserkt/pat/ext/MiscHelper.kt#L80), and `class ConvertAs` [source](https://github.com/ParserKt/ParserKt/blob/master/src/commonMain/kotlin/org/parserkt/pat/WrapperCCDP.kt#L19)

```kotlin
fun digitFor(cs: CharRange, zero: Char = '0', pad: Int = 0): Convert<Char, Char, Int>
  = Convert(elementIn(cs), { (it - zero) +pad }, { zero + (it -pad) })
//^ a bit complicated

data class NameWrapper(override val v: String): ConvertAs.Box<String>
val name = Repeat(asString(), elementIn('a'..'z')) typed ::NameWrapper

name.read("nihao") //NameWrapper(v=nihao)
name.rebuild("nihao") //nihao

name.force<Char, NameWrapper/*T:T1*/, Any/*T1*/>().read("emmm") //NameWrapper(v=nihao) //Any

// force() pattern must run force-cast on show(v: Any)
```

> See [hanCalc:Calc.kt](https://github.com/ParserKt/examples/blob/master/hanCalc/src/commonMain/kotlin/Calc.kt#L15) for more

```kotlin
val sign = elementIn('+', '-').toDefault('+')
val strPart = LexicalBasics.stringFor(anyChar)
val str = Contextual(sign) { sign ->
  if (sign != '+') Piped(strPart) { it?.reversed() }
  else strPart
}.discardFirst() //first=sign

str.read("haha") //haha
str.read("-haha") //ahah
```

> The main usage for `Deferred` is to create recursive syntax

```kotlin
// infite parens for (a)
lateinit var a: Pattern<Char, String>
val str = LexicalBasics.stringFor(!item(')'))
val paren = SurroundBy(parens.toCharPat(), Deferred{a})
a = Decide(paren, str).discardFirst()

a.read("((hello))") //hello
a.read("((a") //""
//^ Decide fall-thru to str when ')' is missing, make right-paren clam() can produce explicit error
```

### Complex patterns "SJIT"

> "SJIT" means SurroudBy, JoinBy, InfixPattern, TriePattern (`pat/complex/`) [AuxiliarySJ.kt](https://github.com/ParserKt/ParserKt/blob/master/src/commonMain/kotlin/org/parserkt/pat/complex/AuxiliarySJ.kt) [InfixPattern](https://github.com/ParserKt/ParserKt/blob/master/src/commonMain/kotlin/org/parserkt/pat/complex/InfixPattern.kt) [TriePattern.kt](https://github.com/ParserKt/ParserKt/blob/master/src/commonMain/kotlin/org/parserkt/pat/complex/TriePattern.kt)

#### SurroundBy

> DSL operations: `Pattern.prefix`, `Pattern.suffix`

```kotlin
val dot2 = item('.') suffix item('.').toDefault()

dot2.read(".") == dot2.read("..") //true
dot2.rebuild(".") //..
```

> NOTE: `OptionalPattern` is not `ConstantPattern`, constant pattern does not always parsed

#### JoinBy

```kotlin
val comma = item(',')
val list = JoinBy(comma, Repeat(asString(), !comma)).mergeConstantJoin()
//^ NOTE: mergeConstantJoin supports constant item or depth-1 PatternWrapper

list.read("hello,world") //[hello, world]
```

> Extensions: `mergeConstantJoin(value)`, `mergeConstantJoin()`, `concatCharJoin()`. [source](https://github.com/ParserKt/ParserKt/blob/master/src/commonMain/kotlin/org/parserkt/pat/complex/AuxiliarySJ.kt#L84)

#### Rescue and OnItem

> See [hanCalc:Calc.kt](https://github.com/ParserKt/examples/blob/master/hanCalc/src/commonMain/kotlin/Calc.kt#L48)

```kotlin
//incomplete code
val duoLine = int prefix item('\n')
expr = InfixPattern(atom, ops).Rescue { s, base, op1 ->
  print("|")
  duoLine.read(s) ?: notParsed.also { s.error("expecting rhs for $base $op1") }
}

// ...in function main
fun ps1() = print("> ")
val input = CharInput.STDIN

val line = Decide(expr,
  StickyEnd(EOF, 233)).discardFirst()
val repl = JoinBy(item('\n'), line)
  .OnItem { println("= $it"); ps1() }
  .mergeConstantJoin()
```

```bash
java -cp build/libs/Examples-s.jar example.HanCalc
> 360*
|3
= 1080
> 一百加
|三十乘
|五
= 250
> = 233
> [1080, 250, 233]
```

#### InfixPattern and TriePattern

```kotlin
object Calc: LexicalBasics() {
  val ops = KeywordPattern<InfixOp<Int>>().apply {
    register("+" infixl 0 join Int::plus)
    register("-" infixl 0 join Int::minus)
  }
  val expr = InfixPattern(numInt, ops)
}

Calc.expr.read("1+2-3") //0
Calc.expr.read("3+1") //4
Calc.expr.show(9) //!UnsupportedOperationException: infix show

// Precedence(-1, isRAssoc=false)
// InfixOp<Int>("*", prec, Int::times)
Calc.ops["*"] = "*" infixl (-1) join Int::times

Calc.expr.read("1+2*3") //7
```

And we can also create a parser for right-associative chain:

```kotlin
sealed class Type {
  data class Named(override val v: String): Type(), ConvertAs.Box<String>
  data class Fn(val arg: String, val res: Type): Type()
}

val ops = KeywordPattern<InfixOp<Type>>().apply {
  register("->" infixr 0 join { t, r -> Type.Fn((t as Type.Named).v, r) })
}
val upcase = elementIn('A'..'Z')
val lowcase = elementIn('a'..'z')

val typeId = LexicalBasics.run { prefix1(upcase, stringFor(upcase or lowcase)) }
val _atomType = typeId typed { Type.Named(it) }
val atomType: Pattern<Char, Type> = _atomType.force()

val expr = InfixPattern(atomType, ops)
```

```kotlin
expr.read("User") //Named(v=User)
expr.read("Int->String->String") //Fn(arg=Int, res=Fn(arg=String, res=Named(v=String)))
```

See [Parserkt/README.md](https://github.com/ParserKt/ParserKt#more-runnable-repl-snippet) for examples about `TriePattern`.

For detailed information about `MapPattern`/`TriePattern`/`PairedTriePattern`, see [source](https://github.com/ParserKt/ParserKt/blob/master/src/commonMain/kotlin/org/parserkt/pat/complex/TriePattern.kt).

> NOTE: In ParserKt, bi-directional mappings are __UNCHECKED__, taking reverse map for single-value-multiply-key `Map` is undefined behavior

### About `mergeXXX`

<a id="About_mergeXXX">ParserKt</a> have many `Pattern<IN, Tuple2<A, B>>` — e.g. `Decide: ...<Int/*CaseNo*/, T>`, `Contextual: ...<A, B>`, `JoinBy: ...<ITEM, SEP>`

But it's too complicated to create storage for them! So we can use `Convert` to extract only a certain part from `read` result.

```kotlin
val abc123 = Decide(elementIn('1'..'3'), elementIn('a'..'c'))
val got = Convert(abc123, { it.second }, { Tuple2( if (it in '1'..'3') 0 else 1, it) })

got.read("a") //a //Char
got.rebuild("a") //a //String
```

Why not to use `mergeFirst`/`mergeSecond`? And we can also apply `discardFirst`/`discardSecond` for patterns that has no need to keep original informating

```kotlin
val abc123 = Decide(elementIn('1'..'3'), elementIn('a'..'c')).mergeFirst { if (it in '1'..'3') 0 else 1 }
```

— e.g. signed integer can be outputed directly via `toString`, without recursively call `Pattern.show`. [showBy](https://github.com/ParserKt/ParserKt/blob/master/src/commonMain/kotlin/org/parserkt/pat/PatternModel.kt#L158) could be used for this approach.

```kotlin
val int = Repeat(asInt(), LexicalBasics.digitFor('0'..'9')).showByString { it.toString() }
int.read("12345") //12345 //Int
int.rebuild("12345") //12345 //String
```

There are also a operation named `flatten` [source](https://github.com/ParserKt/ParserKt/blob/master/src/commonMain/kotlin/org/parserkt/pat/WrapperCCDP.kt#L68)

```kotlin
// pattern [a, a]
val i2 = Seq(::IntTuple, *Contextual(item<Int>()) { item(it) }.flatten().items())

i2.read(1,2) //notParsed
i2.read(1,1) //(1, 1) //IntTuple
i2.rebuild(1,1) //[1, 1] //List<Int>
```

### Clam — Error handling

> See [PatternMisc.kt](https://github.com/ParserKt/ParserKt/blob/master/src/commonMain/kotlin/org/parserkt/pat/PatternMisc.kt#L9)

Using `Input<IN>.addErrorList()` / `CharInput.addErrorList()`, or (`SatisfyPattern`/`SatisfyEqualToPattern`).`clam(messager)`

```kotlin
val (es, input) = inputOf("123").addErrorList()
input //Input:Slice('1'...123):<string>:1:0#0
elementIn('a'..'c').clam {"expecting [a-c]"}.read(input) //notParsed
es //[(<string>:1:0#0, expecting [a-c])]
es[0].first.tag //<string>:1:0
```

```kotlin
val (es, input) = inputOf("hello", "world!", "---").addErrorList()
input //Input:Slice("hello"...hello, |...
satisfy<String>("..!") { it.endsWith("!") }.clam().read(input) //world!
es //[(hello, expecting (..!))]
```

Using `Pattern.clamWhile` and `Feed.clamWhile`

```kotlin
val num = Repeat(asList(), elementIn('0'..'9')).clamWhile(item(' '), emptyList()) {"number required"}
num.readPartial("233") //([], [2, 3, 3])
num.readPartial("   666") //([(<string>:1:0#0, number required)], [])
```

we can make a pattern skip something when it fails to match

```kotlin
val num = Repeat(asString(), elementIn('0'..'9'))
val str=Repeat(asString(), elementIn('a'..'z', 'A'..'Z'))

val element = Decide(num, str.clamWhile(!item(','), "") {"expecting element"}).discardFirst()

JoinBy(item(','), element).mergeConstantJoin().readPartial("123,abc,-4232,a")
//([(<string>:1:8#8, expecting element)], [123, abc, , a])
```

you can also use it with pseudo pattern `never()` (never parsed) and `always(value)`.

### Storing context in `Feed`s

> See [PatternMisc.kt: interface State](https://github.com/ParserKt/ParserKt/blob/master/src/commonMain/kotlin/org/parserkt/pat/PatternMisc.kt#L55)

Use `Input<IN>.withState(value)` or `CharInput.withState(value)` to create wrapper feed with certain `value` associated

ParserKt provides many `Pattern` receiving `Feed<IN>.() -> ...` as argument, use `AllFeed.stateAs<ST>(): ST?` to acquire state from `Feed<*>` instance.

#### Modifing state with parse result

Simply use (`Pattern`/`SatisfyPattern`/`SatisfyEqualTo`).`alsoDo(ConsumerOn<AllFeed, T>)`

```kotlin
fun AllFeed.notInState(item: Char) = stateAs<Set<Char>>()?.contains(item)?.not() ?: true
val differentChar = Piped(anyChar) { res -> res?.takeIf { notInState(it) } }.alsoDo { stateAs<MutableSet<Char>>()?.add(it) }

val differentChars = Repeat(asString(), differentChar)

differentChars.read(inputOf("asdf").withState(mutableSetOf<Char>())) //asdf
differentChars.read(inputOf("asdfae").withState(mutableSetOf<Char>())) //asdf
```

### Operation on `Feed`s

> See [FeedModel.kt](https://github.com/ParserKt/ParserKt/blob/master/src/commonMain/kotlin/org/parserkt/FeedModel.kt), there are `SliceFeed` and `StreamFeed` (`IteratorFeed`, `ReaderFeed`)

Consume operations e.g. `consumeOrNull`(null when <abbr title="end-of-stream">EOS</abbr>), `consumeIf`, `takeWhileNotEnd`

> `takeWhile` will throw `Feed.End` when EOS encountered

Helper operations: `isStickyEnd` (has side-effect, see [source](https://github.com/ParserKt/ParserKt/blob/master/src/commonMain/kotlin/org/parserkt/FeedModel.kt#L32) for detail), `catchError(Producer)`

Convert operations: `asSequence`, `asIterable`

Collect operations: `toList`, `readText`

### `Pattern.toXXX`

#### `toConstant` and `toDefault`

> See [PatternModel.kt](https://github.com/ParserKt/ParserKt/blob/master/src/commonMain/kotlin/org/parserkt/pat/PatternModel.kt#L68)

`ConstantPattern` are patterns e.g. `item('\n')` with constant values can be ignored in read result (without losing required information for running `rebuild`)

```kotlin
val NL = elementIn('\r', '\n').toConstant('\n')
val white = elementIn(' ', '\t').toConstant(' ')
```

`OptionalPattern` are patterns with default values, since `notParsed` is identical to `null` and ParserKt encodes match success/fail using value nullability, `OptionalPattern` always result non-null

```kotlin
val sign = elementIn('+', '-').toDefault('+')
```

"POPCorn" (rest 3 upper-cased character denotes `OptionalPattern`, `PatternWrapper`, `ConstantPattern`)

#### `toXXXPat`

> See [CombSURD.kt's ext funs after RepeatUn](https://github.com/ParserKt/ParserKt/blob/master/src/commonMain/kotlin/org/parserkt/pat/CombSURD.kt#L132)

```kotlin
typealias MonoPair<T> = Pair<T, T>
//^ "mono" means "pair of same thing" in this project

MonoPair<T>.toPat()
MonoPair<String>.toCharPat()
//^ must be single-char string
MonoPattern<Char>.toStringPat()

Pattern<IN, Int>.toLongPat()
Seq<IN=Char, Char, CharTuple>.toStringPat()
//^ e.g. Seq(::CharTuple, anyChar, anyChar).toStringPat()
```

### Using `org.parserkt.pat.ext`

> `NumUnit`, `LayoutPattern`, `DictTrie`, `GreedyPairedTrie`, ... [source](https://github.com/ParserKt/ParserKt/tree/master/parserkt-ext/src/commonMain/kotlin/org/parserkt/pat/ext)

```kotlin
val num = RepeatUn(asInt(), LexicalBasics.digitFor('0'..'9')) { it.toString().map { it-'0' } }
val timeUnit = PairedKeywordPattern<Int>().apply { mergeStrings("s" to 1, "min" to 60, "hr" to 60*60) }
val time = TrieNumUnit(num, timeUnit, IntOps)
```

```kotlin
time.read("1hr") //3600
time.show(60) //1min
```

For more examples of `pat.ext`, please view source files in [extendedSyntax](extendedSyntax)
+ `LayoutPattern` can be used for parsing Python-like `def :` , `if :` indentation-based blocks
+ `DictTrie` is a kind of `BackTrie` (also `PairedTriePattern`) with `back` reversed trie pattern
+ `GreedyPairedTrie` is a kind of `LazyPairedTrie`, it will translate all known words(`String`) to word-unknown-word list without tokenizer required

#### _for more documents, see [ParserKt wiki](https://github.com/ParserKt/ParserKt/wiki/)_
