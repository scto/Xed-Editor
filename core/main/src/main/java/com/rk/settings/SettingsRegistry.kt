package com.rk.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController

data class SettingsCategory(
    val labelRes: Int,
    val descriptionRes: Int,
    val iconRes: Int,
    val route: String,
)

data class SettingsRoute(
    val route: String,
    val arguments: List<NamedNavArgument> = emptyList(),
    val content: @Composable (NavController, NavBackStackEntry) -> Unit,
)

object SettingsRegistry {

    private val _categories = mutableStateListOf<SettingsCategory>()
    val categories: List<SettingsCategory>
        get() = _categories.toList()

    private val _routes = mutableStateListOf<SettingsRoute>()
    val routes: List<SettingsRoute>
        get() = _routes.toList()

    fun registerCategory(category: SettingsCategory) {
        _categories.add(category)
    }

    fun registerRoute(route: SettingsRoute) {
        _routes.add(route)
    }
}
