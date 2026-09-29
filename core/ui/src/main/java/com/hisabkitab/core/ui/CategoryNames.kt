package com.hisabkitab.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.hisabkitab.core.model.Category

/** Translations for the built-in categories, keyed by [Category.defaultKey]. */
private val DefaultCategoryNames: Map<String, Int> = mapOf(
    "food" to R.string.category_food,
    "shopping" to R.string.category_shopping,
    "transportation" to R.string.category_transportation,
    "bills" to R.string.category_bills,
    "home" to R.string.category_home,
    "health" to R.string.category_health,
    "entertainment" to R.string.category_entertainment,
    "education" to R.string.category_education,
    "travel" to R.string.category_travel,
    "clothing" to R.string.category_clothing,
    "fruits" to R.string.category_fruits,
    "vegetables" to R.string.category_vegetables,
    "car" to R.string.category_car,
    "insurance" to R.string.category_insurance,
    "gift" to R.string.category_gift,
    "sport" to R.string.category_sport,
    "book" to R.string.category_book,
    "pet" to R.string.category_pet,
    "wine" to R.string.category_wine,
    "salary" to R.string.category_salary,
    "rental" to R.string.category_rental,
    "sale" to R.string.category_sale,
    "awards" to R.string.category_awards,
    "investment" to R.string.category_investment,
    "other" to R.string.category_other,
)

/** The category's name in the user's language (built-in ones are translated; custom ones as typed). */
@Composable
fun Category.displayName(): String = defaultKey?.let(DefaultCategoryNames::get)?.let { stringResource(it) } ?: name
