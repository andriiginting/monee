package data

import kotlinx.serialization.Serializable

@Serializable
data class BalanceData(
    val totalBalance: Int,
    val currency: CurrencyType,
    var isVisibleToUser: Boolean = false,
)

@Serializable
enum class CurrencyType(val symbol: String) {
    JPY("¥"),
    IDR("Rp.")
}
