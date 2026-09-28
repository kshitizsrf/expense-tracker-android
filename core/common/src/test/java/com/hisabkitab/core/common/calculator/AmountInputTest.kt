package com.hisabkitab.core.common.calculator

import com.hisabkitab.core.common.calculator.AmountInput.MINUS
import com.hisabkitab.core.common.calculator.AmountInput.PLUS
import com.hisabkitab.core.common.calculator.AmountInput.TIMES
import org.junit.Assert.assertEquals
import org.junit.Test

class AmountInputTest {

    private fun type(vararg keys: Char): String = keys.fold("") { expression, key ->
        when {
            key.isDigit() -> AmountInput.appendDigit(expression, key)
            key == '.' -> AmountInput.appendDecimalPoint(expression)
            else -> AmountInput.appendOperator(expression, key)
        }
    }

    @Test
    fun appendsDigits() = assertEquals("125", type('1', '2', '5'))

    @Test
    fun replacesLeadingZero() = assertEquals("5", type('0', '5'))

    @Test
    fun keepsSingleZero() = assertEquals("0", type('0', '0'))

    @Test
    fun decimalPointOnEmptyInputGetsLeadingZero() = assertEquals("0.5", type('.', '5'))

    @Test
    fun onlyOneDecimalPointPerNumber() = assertEquals("1.5", type('1', '.', '.', '5'))

    @Test
    fun limitsToTwoDecimalPlaces() = assertEquals("1.25", type('1', '.', '2', '5', '9'))

    @Test
    fun eachNumberGetsItsOwnDecimalPoint() = assertEquals("1.5${PLUS}2.5", type('1', '.', '5', PLUS, '2', '.', '5'))

    @Test
    fun operatorCannotStartExpression() = assertEquals("", type(PLUS))

    @Test
    fun consecutiveOperatorsReplaceEachOther() = assertEquals("5$TIMES", type('5', PLUS, MINUS, TIMES))

    @Test
    fun operatorAfterDecimalPointDropsThePoint() = assertEquals("5$PLUS", type('5', '.', PLUS))

    @Test
    fun limitsIntegerDigits() = assertEquals("123456789", type('1', '2', '3', '4', '5', '6', '7', '8', '9', '0'))

    @Test
    fun backspaceRemovesLastCharacter() = assertEquals("12", AmountInput.backspace("125"))

    @Test
    fun detectsOperators() {
        assertEquals(true, AmountInput.hasOperator("1${PLUS}2"))
        assertEquals(false, AmountInput.hasOperator("12.5"))
    }
}
