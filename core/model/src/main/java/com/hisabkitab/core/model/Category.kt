package com.hisabkitab.core.model

/**
 * A user-defined bucket for transactions.
 *
 * @property iconKey stable key of a bundled icon (see `CategoryIcons`).
 * @property color ARGB color used for the icon badge and charts.
 */
data class Category(
    val id: Long,
    val name: String,
    val iconKey: String,
    val color: Int,
    val type: TransactionType,
    /** Set for the built-in categories so their names can be shown in the user's language. */
    val defaultKey: String? = null,
)

/** Colors offered in the category editor; mid-tones that read well on light and dark surfaces. */
object CategoryColors {
    val all: List<Int> = listOf(
        0xFFE53935, // red
        0xFFD81B60, // pink
        0xFF8E24AA, // purple
        0xFF5E35B1, // deep purple
        0xFF3949AB, // indigo
        0xFF1E88E5, // blue
        0xFF039BE5, // light blue
        0xFF00897B, // teal
        0xFF43A047, // green
        0xFF7CB342, // light green
        0xFFF9A825, // amber
        0xFFFB8C00, // orange
        0xFFF4511E, // deep orange
        0xFF6D4C41, // brown
        0xFF546E7A, // blue grey
    ).map { it.toInt() }

    fun forIndex(index: Int): Int = all[Math.floorMod(index, all.size)]
}
