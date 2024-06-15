package navigation

internal enum class Navigator(val route: String) {
    HOME("/home"),
    HISTORY("/history"),
    SPLASH("/splash"),
    PERSONALIZE_CARD("/personalize_card");

    companion object {
        fun getTopLevelRoute(): List<Navigator> {
            return listOf(
                HOME,
                HISTORY
            )
        }
    }
}