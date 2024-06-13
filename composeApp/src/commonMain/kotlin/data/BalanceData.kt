package data

data class BalanceData(
    val totalBalance: Int,
    val currency: CurrencyType,
    var isVisibleToUser: Boolean = false,
)

enum class CurrencyType(val symbol: String) {
    JPY("¥"),
    IDR("Rp.")
}