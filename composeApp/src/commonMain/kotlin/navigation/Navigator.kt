package navigation

internal enum class Navigator(val route: String) {
    HOME("/home"),
    HISTORY("/history"),
    BUDGET("/budget"),
    INSIGHT("/insight"),
    SPLASH("/splash"),
    SCANNER("/scanner"),
    PERSONALIZE_CARD("/personalize_card");

    companion object {
        fun getTopLevelRoute(): List<Navigator> {
            return listOf(
                HOME,
                HISTORY,
                BUDGET,
                INSIGHT
            )
        }
    }
}
