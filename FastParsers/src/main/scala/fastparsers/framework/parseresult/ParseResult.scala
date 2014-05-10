package fastparsers.framework.parseresult

/**
 *
 * @param success If the result is a success
 * @param error The error message if any (only relevant if success == false)
 * @param result The result generated by the parser
 * @param inputPos The position in the fastparsers.input at which the parser has finished to read
 * @tparam T Type of the result
 * @tparam U Type of the error
 */
case class ParseResult[+T,U](success: Boolean, error: U, result: T, inputPos: Int)
