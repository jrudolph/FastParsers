
/**
 *
 * @param success If the result is a success
 * @param msg The error message if any (only relevant if success == false)
 * @param result The result generated by the parser
 * @param inputPos The position in the input at which the parser has finished to read
 * @tparam T Type of the result
 */
case class ParseResult[+T](success: Boolean, msg: String, result: T, inputPos: Int)

/**
 * Extractor for ParseResult in case of success
 */
object Success {
  def unapply[T](p: ParseResult[T]) =
    if (p.success) Some(p.result)
    else None
}

/**
 * Extractor for ParseResult in case of Failure
 */
object Failure {
  def unapply[T](p: ParseResult[T]) =
    if (!p.success) Some(p.msg)
    else None
}

