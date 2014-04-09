import scala.annotation.compileTimeOnly
import scala.language.implicitConversions
import scala.collection.mutable.ListBuffer
import scala.reflect.macros.whitebox.Context

/**
 * Base Parsers
 */

trait BaseParsers[Elem, Input] {

  import InputWindow._

  object ~ {
    def unapply[T, U](x: Tuple2[T, U]): Option[Tuple2[T, U]] = Some((x._1, x._2))
  }

  abstract class ElemOrRange

  @compileTimeOnly("can’t be used outside FastParser")
  implicit def toElemOrRange(elem: Elem): ElemOrRange = ???

  @compileTimeOnly("can’t be used outside FastParser")
  implicit def toElemOrRange(elem: (Elem,Elem)): ElemOrRange = ???




  @compileTimeOnly("can’t be used outside FastParser")
  implicit def toElem(elem: Elem): Parser[Elem] = ???

  @compileTimeOnly("can’t be used outside FastParser")
  implicit def toElem(elem: (Elem, Elem)): Parser[Elem] = ???

  @compileTimeOnly("can’t be used outside FastParser")
  def range(a: Elem, b: Elem): Parser[Elem] = ???

  @compileTimeOnly("can’t be used outside FastParser")
  def accept(p1: ElemOrRange, p2: ElemOrRange*):Parser[Elem] = ???

  @compileTimeOnly("can’t be used outside FastParser")
  def not(p1: ElemOrRange, p2: ElemOrRange*): Parser[Elem] = ???

  @compileTimeOnly("can’t be used outside FastParser")
  def acceptIf(f: Elem => Boolean): Parser[Elem] = ???

  @compileTimeOnly("can’t be used outside FastParser")
  def wildcard: Parser[Elem] = ???

  @compileTimeOnly("can’t be used outside FastParser")
  def takeWhile(f: Elem => Boolean): Parser[Input] = ???

  @compileTimeOnly("can’t be used outside FastParser")
  def take(n: Int): Parser[Input] = ???

  @compileTimeOnly("can’t be used outside FastParser")
  def raw[T](p:Parser[T]):Parser[InputWindow[Input]] = ???

  @compileTimeOnly("can’t be used outside FastParser")
  def guard[T](p: Parser[T]): Parser[T] = ???

  @compileTimeOnly("can’t be used outside FastParser")
  def phrase[T](p: Parser[T]): Parser[T] = ???

  @compileTimeOnly("can’t be used outside FastParser")
  def failure(msg: String): Parser[Any] = ???

  @compileTimeOnly("can’t be used outside FastParser")
  def success[T](v: T): Parser[T] = ???

  @compileTimeOnly("can’t be used outside FastParser")
  def ifelse[T](cond: Boolean,thn: Parser[T], els: Parser[T]): Parser[T] = ???

  trait BaseParser[T] {
    @compileTimeOnly("can’t be used outside FastParser")
    def ~[U](p2: Parser[U]): Parser[(T, U)] = ???

    @compileTimeOnly("can’t be used outside FastParser")
    def ~>[U](p2: Parser[U]): Parser[U] = ???

    @compileTimeOnly("can’t be used outside FastParser")
    def <~[U](p2: Parser[U]): Parser[T] = ???

    @compileTimeOnly("can’t be used outside FastParser")
    def ||[U >: T](p2: Parser[U]): Parser[U] = ???

    @compileTimeOnly("can’t be used outside FastParser")
    def |[U >: T](p2: Parser[U]): Parser[U] = ???

    @compileTimeOnly("can’t be used outside FastParser")
    def ^^[U](f: T => U): Parser[U] = ???

    @compileTimeOnly("can’t be used outside FastParser")
    def map[U](f: T => U): Parser[U] = ???

    @compileTimeOnly("can’t be used outside FastParser")
    def ^^^[U](f: U): Parser[U] = ???

    @compileTimeOnly("can’t be used outside FastParser")
    def filter[U >: T](f: T => Boolean): Parser[T] = ???

    @compileTimeOnly("can’t be used outside FastParser")
    def withFailureMessage(msg: String): Parser[T] = ???
  }

  implicit class elemParser(p1: Elem) extends BaseParser[Elem]
  implicit class baseParsers[T](p1: Parser[T]) extends BaseParser[T]

}


/**
 * Expansion of Basic combinators
 */
trait BaseParsersImpl extends CombinatorImpl {
  self: ParseInput =>

  import c.universe._

  override def expand(tree: c.Tree, rs: ResultsStruct) = tree match {
    case q"FastParsers.baseParsers[$d]($a)" => expand(a, rs)
    case q"FastParsers.toElem(($a,$b))"     => parseRange(a, b, rs)
    case q"FastParsers.toElem($elem)"       => parseElem(elem, rs)
    case q"FastParsers.elemParser($elem)"   => parseElem(elem, rs)
    case q"FastParsers.range($a,$b)"        => parseRange(a, b, rs)
    case q"FastParsers.accept(..$a)"        => parseAccept(a,false,rs)
    case q"FastParsers.not(..$a)"           => parseAccept(a,true,rs)
    case q"FastParsers.acceptIf($f)"        => parseAcceptIf(f, rs)
    case q"FastParsers.wildcard"            => parseWildcard(rs)
    case q"FastParsers.guard[$d]($a)"       => parseGuard(a, d, rs)
    case q"FastParsers.takeWhile($f)"       => parseTakeWhile(f, rs)
    case q"FastParsers.take($n)"            => parseTake(n, rs)
    case q"FastParsers.raw[$d]($a)"         => parseRaw(a,rs)
    case q"FastParsers.phrase[$d]($a)"      => parsePhrase(a, rs)
    case q"FastParsers.failure($a)"         => parseFailure(a,rs)
    case q"FastParsers.success[$d]($a)"     => parseSuccess(a,d,rs)
    case q"$a ~[$d] $b"                     => parseThen(a, b, rs)
    case q"$a ~>[$d] $b"                    => parseIgnoreLeft(a, b, d, rs)
    case q"$a <~[$d] $b"                    => parseIgnoreRight(a, b, d, rs)
    case q"$a ||[$d] $b"                    => parseOr(a, b, d, rs)
    case q"$a |[$d] $b"                     => parseOr(a, b, d, rs)
    case q"$a ^^[$d] $f"                    => parseMap(a, f, d, rs)
    case q"$a map[$d] $f"                   => parseMap(a, f, d, rs)
    case q"$a ^^^[$d] $v"                   => parseValue(a, v, d, rs)
    case q"$a filter[$d] $f"                => parseFilter(a, f, d, rs)
    case q"$a withFailureMessage $msg"      => parseWithFailureMessage(a, msg, rs)
    case q"call[$d](${ruleCall: TermName})" => parseRuleCall(ruleCall, d, rs)
    case q"compound[$d]($a)"                => parseCompound(a, d, rs)
    case q"FastParsers.ifelse[$d]($cond,$a,$b)"         => parseIfThnEls(cond,a,b,d,rs)
    case _                                  => super.expand(tree, rs) //q"""println(show(reify($tree).tree))"""
  }


  private def parseElem(a: c.Tree, rs: ResultsStruct) = {
    val result = TermName(c.freshName)
    rs.append((result, inputElemType, true))
    q"""
      if ($isNEOI && $currentInput == $a){
        $result = $a
        $advance
        success = true
       }
       else {
          success = false
          msg = "expected '" + $a + " at " + $pos
        }
     """
  }

  private def parseRange(a: c.Tree, b: c.Tree, rs: ResultsStruct) = {
    val result = TermName(c.freshName)
    rs.append((result, inputElemType, true))
    q"""
     if ($isNEOI && $currentInput >= $a && $currentInput <= $b){
      $result = $currentInput
      $advance
      success = true
     }
     else {
        success = false
        msg = "expected in range ('" + $a + "', '" + $b + "')  at " + $pos
     }
    """
  }

  private def parseAccept(a: List[c.Tree],negate: Boolean,rs: ResultsStruct) = {
    val result = TermName(c.freshName)
    rs.append((result, inputElemType, true))
    val ranges = if (negate)  q"!(${getAcceptedElem(a)})"
                 else q"(${getAcceptedElem(a)})"
    q"""
      if ($isNEOI && $ranges){
        $result = $currentInput
        $advance
        success = true
       }
       else {
          success = false
          msg = "expected TODO at " + $pos
        }
     """ //TODO error msg
  }

  private def getAcceptedElem(elems: List[c.Tree]) = {
    def acceptElem(elem: c.Tree) = elem match {
      case q"FastParsers.toElemOrRange(($x,$y))"  => q"($currentInput >= $x && $currentInput <= $y)"
      case q"FastParsers.toElemOrRange($x)"       => q"$currentInput == $x"
    }
    elems.tail.foldLeft(q"${acceptElem(elems.head)}"){(acc, c) => q"$acc || ${acceptElem(c)}"}
  }

  private def parseAcceptIf(f: c.Tree, rs: ResultsStruct) = {
    val result = TermName(c.freshName)
    rs.append((result, inputElemType, true))
    q"""
     if ($isNEOI && $f($currentInput)){
      $result = $currentInput
      $advance
      success = true
     }
     else {
        success = false
        msg = "acceptIf combinator failed at " + $pos
     }
     """
  }

  private def parseWildcard(rs: ResultsStruct) = {
    val result = TermName(c.freshName)
    rs.append((result, inputElemType, true))
    q"""
      if ($isNEOI){
        $result = $currentInput
        $advance
        success = true
      }
      else
        success = false

    """
  }


  private def parseGuard(a: c.Tree, typ: c.Tree, rs: ResultsStruct) = {
    val tree = mark {
      rollback =>
        q"""
         ${expand(a, rs)}
         $rollback
       """
    }
    tree
  }


  private def parseTakeWhile(f: c.Tree, rs: ResultsStruct) = {
    val result = TermName(c.freshName)
    val tmp_f = TermName(c.freshName)
    val beginpos = TermName(c.freshName)
    rs.append((result, inputType, true))
    q"""
      val $tmp_f = $f
      val $beginpos = $pos
      while ($isNEOI && $tmp_f($currentInput))
        $advance
      $result = ${slice(q"$beginpos", q"$pos")}
      success = true
    """
  }

  private def parseTake(n: c.Tree, rs: ResultsStruct) = {
    val result = TermName(c.freshName)
    rs.append((result, inputType, true))
    q"""
    if ($pos + $n <= $inputsize) {
      success = true
      $result = ${slice(pos, q"$pos + $n")}
    }
    else {
      success = false
      msg = "take(" + $n + ") cannot proceed, only " + ($inputsize - $pos) + " elements left at " + $pos
    }
    """
  }

  private def parseRaw(a: c.Tree, rs: ResultsStruct) = {
    val beginpos = TermName(c.freshName)
    val result = TermName(c.freshName)
    val results_tmp = new ResultsStruct()
    //${slice(q"$beginpos", q"$pos")}
    val tree = q"""
      val $beginpos = $pos
      ${expand(a,results_tmp)}
      if (success) {
        $result = new InputWindow(input,$beginpos,$pos)
      }
      else {
        msg = "raw failure"
      }
    """
    results_tmp.setNoUse
    rs.append(results_tmp)
    rs.append((result, tq"InputWindow[$inputType]", true))
    tree
  }

  private def parsePhrase(a: c.Tree, rs: ResultsStruct) = {
    q"""
    ${expand(a, rs)}
    if (success) {
      if (!$isEOI){
        success = false
        msg = "not all the input is consummed, at pos " + $pos
      }
    }
    """
  }


  private def parseFailure(a: c.Tree, rs: ResultsStruct) = {
    q"""
      success = false
      msg = $a
    """
  }
  private def  parseSuccess(a: c.Tree,typ: c.Tree, rs: ResultsStruct) = {
    val result = TermName(c.freshName)
    rs.append((result, typ, true))
    q"""
      success = true
      $result = $a
    """
  }


  private def parseThen(a: c.Tree, b: c.Tree, rs: ResultsStruct) = {
    q"""
      ${expand(a, rs)}
      if (success) {
        ${expand(b, rs)}
      }
   """
  }

  private def parseIgnoreLeft(a: c.Tree, b: c.Tree, typ: c.Tree, rs: ResultsStruct) = {
    val results_tmp = new ResultsStruct()
    val tree =
      q"""
      ${expand(a, results_tmp)}
      if (success) {
        ${expand(b, rs)}
      }
     """
    results_tmp.setNoUse
    rs.append(results_tmp)
    tree
  }

  private def parseIgnoreRight(a: c.Tree, b: c.Tree, typ: c.Tree, rs: ResultsStruct) = {
    val results_tmp = new ResultsStruct()
    val tree =
      q"""
      ${expand(a, rs)}
      if (success) {
        ${expand(b, results_tmp)}
      }
     """
    results_tmp.setNoUse
    rs.append(results_tmp)
    tree
  }

  def parseOr(a: c.Tree, b: c.Tree, typ: c.Tree, rs: ResultsStruct) = {
    val result = TermName(c.freshName)
    var results_tmp1 = new ResultsStruct()
    var results_tmp2 = new ResultsStruct()
    val tree = mark {
      rollback =>
        q"""
          ${expand(a, results_tmp1)}
          if (!success) {
            $rollback
            ${expand(b, results_tmp2)}
            if (success)
              $result = ${combineResults(results_tmp2)}
          }
          else {
            $result = ${combineResults(results_tmp1)}
          }
        """
    }

    results_tmp1.setNoUse
    results_tmp2.setNoUse
    rs.append((result, typ, true)) //tq"typ"    Ident(TypeName("Any"))
    rs.append(results_tmp1)
    rs.append(results_tmp2)
    tree
  }

  private def parseMap(a: c.Tree, f: c.Tree, typ: c.Tree, rs: ResultsStruct) = {
    val result = TermName(c.freshName)
    val tmp_f = TermName(c.freshName)
    val results_tmp = new ResultsStruct()
    val tree =
      q"""
      val $tmp_f = $f
      ${expand(a, results_tmp)}
       if (success)
         $result = $tmp_f.apply(${combineResults(results_tmp)})
      """
    results_tmp.setNoUse
    rs.append(results_tmp)
    rs.append((result, typ, true))
    c.untypecheck(tree)
  }

  private def parseValue(a: c.Tree, v: c.Tree, typ: c.Tree, rs: ResultsStruct) = {
    val result = TermName(c.freshName)
    val results_tmp = new ResultsStruct()
    val tree =
      q"""
      ${expand(a, results_tmp)}
       if (success)
         $result = $v
      """
    results_tmp.setNoUse
    rs.append(results_tmp)
    rs.append((result, typ, true))
    tree
  }

  private def parseFilter(a: c.Tree, f: c.Tree, typ: c.Tree, rs: ResultsStruct) = {
    val result = TermName(c.freshName)
    val tmp_f = TermName(c.freshName)
    val results_tmp = new ResultsStruct()
    val tree = mark {
      rollback =>
        q"""
      val $tmp_f = $f
      ${expand(a, results_tmp)}
       if (success && $tmp_f(${combineResults(results_tmp)}))
         $result = ${combineResults(results_tmp)}
       else {
        success = false
        msg = "incorrect result for 'rule' at filter('rule') at " + $pos
        $rollback
       }
      """
    }
    results_tmp.setNoUse
    rs.append(results_tmp)
    rs.append((result, typ, true))
    c.untypecheck(tree)
  }

  private def parseWithFailureMessage(a: c.Tree, msg: c.Tree, rs: ResultsStruct) = {
    q"""
     ${expand(a, rs)}
      if (!success)
         msg = $msg
    """
  }

  private def parseRuleCall(ruleCall: TermName, typ: c.Tree, rs: ResultsStruct) = {
    val callResult = TermName(c.freshName)
    val result = TermName(c.freshName)

    // val $callResult = $ruleCall(input.substring($offset),0)
    //${advanceTo(q"$callResult.inputPos")}
    val tree = q"""
        val $callResult = $ruleCall(input,$offset)
        success = $callResult.success
        if (success){
          ${setpos(q"$callResult.inputPos")}
          $result = $callResult.result
         }
        else
          msg = $callResult.msg
        """
    rs.append((result, typ, true))
    c.untypecheck(tree)
  }

  private def parseCompound(a: c.Tree, typ: c.Tree, rs: ResultsStruct) = {
    val result = TermName(c.freshName)
    var results_tmp = new ResultsStruct()
    val tree =
      q"""
        ${expand(a, results_tmp)}
        $result = ${combineResults(results_tmp)}
      """
    results_tmp.setNoUse
    rs.append((result, typ, true))
    rs.append(results_tmp)
    tree
  }

  private def parseIfThnEls(cond: c.Tree,a: c.Tree,b: c.Tree,typ: c.Tree, rs: ResultsStruct) = {
    val result = TermName(c.freshName)
    var results_tmp1 = new ResultsStruct()
    var results_tmp2 = new ResultsStruct()
    val tree =
    q"""
    if ($cond){
      ${expand(a,results_tmp1)}
      $result = ${combineResults(results_tmp1)}
     }
    else {
      ${expand(b,results_tmp2)}
      $result = ${combineResults(results_tmp2)}
     }
    """
    results_tmp1.setNoUse
    results_tmp2.setNoUse
    rs.append((result, typ, true))
    rs.append(results_tmp1)
    rs.append(results_tmp2)
    tree
  }
}
