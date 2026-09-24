package navigation

internal enum class Navigator(val route: String) {
    HOME("/home"),
    HISTORY("/history"),
    BUDGET("/budget"),
    INSIGHT("/insight"),
    HOUSEHOLD("/household"),
    SPLASH("/splash"),
    AUTH("/auth"),
    SCANNER("/scanner"),
    PERSONALIZE_CARD("/personalize_card");

    companion object {
        fun getTopLevelRoute(): List<Navigator> {
            return listOf(
                HOME,
                HISTORY,
                BUDGET,
                HOUSEHOLD
            )
        }
    }
}
