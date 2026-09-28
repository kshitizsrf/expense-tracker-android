package com.hisabkitab.core.common.money

import com.hisabkitab.core.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.math.BigDecimal
import java.util.Locale

class MoneyTest {

    @Test
    fun convertsToMinorUnitsWithHalfUpRounding() {
        assertEquals(12_550L, BigDecimal("125.5").toMinorUnits())
        assertEquals(1_235L, BigDecimal("12.345").toMinorUnits())
        assertEquals(0L, BigDecimal.ZERO.toMinorUnits())
    }

    @Test
    fun plainAmountStringDropsTrailingZeros() {
        assertEquals("1250", 125_000L.toPlainAmountString())
        assertEquals("12.5", 1_250L.toPlainAmountString())
        assertEquals("0.05", 5L.toPlainAmountString())
        assertEquals("0", 0L.toPlainAmountString())
    }

    @Test
    fun parsesUserInput() {
        assertEquals(125_050L, parseAmountToMinorUnits("1250.50"))
        assertEquals(500L, parseAmountToMinorUnits(" 5 "))
        assertNull(parseAmountToMinorUnits(""))
        assertNull(parseAmountToMinorUnits("abc"))
        assertNull(parseAmountToMinorUnits("-5"))
        assertNull(parseAmountToMinorUnits("99999999999"))
    }

    @Test
    fun formatsInLocaleAndCurrency() {
        val formatter = MoneyFormatter("USD", Locale.US)
        assertEquals("$1,250.50", formatter.format(125_050))
        assertEquals("−$10.00", formatter.formatSigned(1_000, TransactionType.EXPENSE))
        assertEquals("+$10.00", formatter.formatSigned(1_000, TransactionType.INCOME))
        assertEquals("−$3.00", formatter.formatNet(-300))
    }

    @Test
    fun unknownCurrencyFallsBackToRupee() {
        assertEquals("INR", MoneyFormatter("NOT_A_CURRENCY", Locale.US).currency.currencyCode)
    }
}
