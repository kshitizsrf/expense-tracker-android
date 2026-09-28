package com.hisabkitab.core.data.export

import org.junit.Assert.assertEquals
import org.junit.Test

class CsvTest {

    @Test
    fun plainValuesAreUnchanged() = assertEquals("Food", Csv.escape("Food"))

    @Test
    fun commasAreQuoted() = assertEquals("\"Rent, June\"", Csv.escape("Rent, June"))

    @Test
    fun quotesAreDoubled() = assertEquals("\"He said \"\"hi\"\"\"", Csv.escape("He said \"hi\""))

    @Test
    fun newlinesAreQuoted() = assertEquals("\"line1\nline2\"", Csv.escape("line1\nline2"))

    @Test
    fun rowJoinsEscapedValues() = assertEquals("a,\"b,c\",d", Csv.row(listOf("a", "b,c", "d")))
}
