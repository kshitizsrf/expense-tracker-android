package com.hisabkitab.core.common.calculator

import java.math.BigDecimal
import java.math.MathContext

/**
 * Evaluates the calculator expressions typed on the amount keypad, e.g. `120+45×2`.
 *
 * Supports `+ − × ÷` (and their ASCII forms) with the usual precedence. A trailing operator is
 * ignored so a live preview can be shown while typing. Returns null for malformed input or
 * division by zero. Replaces the old Rhino JavaScript engine: tiny, exact (BigDecimal) and safe.
 */
object ExpressionEvaluator {

    fun evaluate(expression: String): BigDecimal? {
        val tokens = tokenize(expression) ?: return null
        val trimmed = if (tokens.lastOrNull() is Token.Operator) tokens.dropLast(1) else tokens
        if (trimmed.isEmpty() || trimmed.first() !is Token.Number) return null

        // First pass: collapse × and ÷ into terms. Second pass: add/subtract the terms.
        val terms = mutableListOf((trimmed[0] as Token.Number).value)
        val additiveOperators = mutableListOf<Char>()
        var index = 1
        while (index < trimmed.size) {
            val operator = trimmed[index] as? Token.Operator ?: return null
            val operand = trimmed.getOrNull(index + 1) as? Token.Number ?: return null
            when (operator.symbol) {
                '*' -> terms[terms.lastIndex] = terms.last().multiply(operand.value, MATH_CONTEXT)
                '/' -> {
                    if (operand.value.signum() == 0) return null
                    terms[terms.lastIndex] = terms.last().divide(operand.value, MATH_CONTEXT)
                }
                else -> {
                    additiveOperators += operator.symbol
                    terms += operand.value
                }
            }
            index += 2
        }

        var result = terms[0]
        additiveOperators.forEachIndexed { i, operator ->
            result = if (operator == '+') result.add(terms[i + 1]) else result.subtract(terms[i + 1])
        }
        return result
    }

    private sealed interface Token {
        data class Number(val value: BigDecimal) : Token
        data class Operator(val symbol: Char) : Token
    }

    private fun tokenize(expression: String): List<Token>? {
        val tokens = mutableListOf<Token>()
        val number = StringBuilder()

        fun flushNumber(): Boolean {
            if (number.isEmpty()) return true
            val text = number.toString()
            number.clear()
            if (text == ".") return false
            tokens += Token.Number(text.toBigDecimalOrNull() ?: return false)
            return true
        }

        for (char in expression) {
            when {
                char.isDigit() || char == '.' -> number.append(char)
                char.isWhitespace() -> Unit
                else -> {
                    val symbol = normalizeOperator(char) ?: return null
                    if (!flushNumber()) return null
                    if (tokens.lastOrNull() is Token.Operator) return null
                    tokens += Token.Operator(symbol)
                }
            }
        }
        return if (flushNumber()) tokens else null
    }

    private fun normalizeOperator(char: Char): Char? = when (char) {
        '+' -> '+'
        '-', '−' -> '-'
        '*', '×' -> '*'
        '/', '÷' -> '/'
        else -> null
    }

    private val MATH_CONTEXT = MathContext.DECIMAL64
}
