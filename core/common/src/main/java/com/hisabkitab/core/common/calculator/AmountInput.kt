package com.hisabkitab.core.common.calculator

/**
 * Pure editing rules for the amount keypad. Each function takes the current expression and
 * returns the new one, rejecting keystrokes that would make it invalid.
 */
object AmountInput {

    const val PLUS = '+'
    const val MINUS = '−'
    const val TIMES = '×'
    const val DIVIDE = '÷'
    val OPERATORS: Set<Char> = setOf(PLUS, MINUS, TIMES, DIVIDE)

    private const val MAX_LENGTH = 32
    private const val MAX_INTEGER_DIGITS = 9
    private const val MAX_FRACTION_DIGITS = 2

    fun appendDigit(expression: String, digit: Char): String {
        require(digit.isDigit()) { "Not a digit: $digit" }
        if (expression.length >= MAX_LENGTH) return expression
        val current = currentNumber(expression)
        if (current == "0") return expression.dropLast(1) + digit
        val dot = current.indexOf('.')
        if (dot >= 0 && current.length - dot - 1 >= MAX_FRACTION_DIGITS) return expression
        if (dot < 0 && current.length >= MAX_INTEGER_DIGITS) return expression
        return expression + digit
    }

    fun appendDecimalPoint(expression: String): String {
        if (expression.length >= MAX_LENGTH) return expression
        val current = currentNumber(expression)
        if ('.' in current) return expression
        return expression + if (current.isEmpty()) "0." else "."
    }

    fun appendOperator(expression: String, operator: Char): String {
        require(operator in OPERATORS) { "Not an operator: $operator" }
        if (expression.isEmpty() || expression.length >= MAX_LENGTH) return expression
        val trimmed = expression.trimEnd('.')
        return when (trimmed.lastOrNull()) {
            null -> expression
            in OPERATORS -> trimmed.dropLast(1) + operator
            else -> trimmed + operator
        }
    }

    fun backspace(expression: String): String = expression.dropLast(1)

    fun hasOperator(expression: String): Boolean = expression.any { it in OPERATORS }

    private fun currentNumber(expression: String): String = expression.takeLastWhile { it !in OPERATORS }
}
