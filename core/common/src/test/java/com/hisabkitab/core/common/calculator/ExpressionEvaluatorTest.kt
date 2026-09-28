package com.hisabkitab.core.common.calculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.math.BigDecimal

class ExpressionEvaluatorTest {

    private fun eval(expression: String): BigDecimal? = ExpressionEvaluator.evaluate(expression)

    private fun assertValue(expected: String, expression: String) {
        val actual = eval(expression)
        requireNotNull(actual) { "Expected $expected for '$expression' but was null" }
        assertEquals(0, BigDecimal(expected).compareTo(actual))
    }

    @Test
    fun plainNumber() = assertValue("125.5", "125.5")

    @Test
    fun respectsOperatorPrecedence() = assertValue("210", "120+45×2")

    @Test
    fun evaluatesLeftToRightForSamePrecedence() = assertValue("5", "10−3−2")

    @Test
    fun supportsAsciiOperators() = assertValue("7.5", "10/4*3")

    @Test
    fun ignoresTrailingOperatorForLivePreview() = assertValue("12", "12+")

    @Test
    fun acceptsLeadingOrTrailingDecimalPoint() {
        assertValue("0.5", ".5")
        assertValue("5", "5.")
    }

    @Test
    fun divisionByZeroIsNull() = assertNull(eval("5÷0"))

    @Test
    fun emptyExpressionIsNull() = assertNull(eval(""))

    @Test
    fun leadingOperatorIsNull() = assertNull(eval("+5"))

    @Test
    fun consecutiveOperatorsAreNull() = assertNull(eval("5+×3"))

    @Test
    fun loneDecimalPointIsNull() = assertNull(eval("."))

    @Test
    fun unknownCharactersAreNull() = assertNull(eval("5+a"))

    @Test
    fun avoidsFloatingPointErrors() = assertValue("0.3", "0.1+0.2")
}
