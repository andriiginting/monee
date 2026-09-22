package navigation

sealed interface AppRoute {
    data object Home : AppRoute
    data object Activity : AppRoute
    data object Budget : AppRoute
    data object Insight : AppRoute
}
