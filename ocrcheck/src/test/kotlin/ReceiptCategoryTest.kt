import data.ocr.CATEGORY_UNKNOWN
import data.ocr.parseReceiptText
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * OCR reports text, not meaning. The category is inferred from the words the
 * receipt itself prints, so these pin the inference against real fixtures and
 * against the case where the receipt says nothing recognisable.
 */
class ReceiptCategoryTest {
    private fun categoryOf(name: String): String {
        val stream = checkNotNull(javaClass.getResourceAsStream("/$name")) { "missing fixture $name" }
        return parseReceiptText(stream.bufferedReader().readText()).category
    }

    @Test
    fun `a restaurant receipt is dining`() {
        // "イタリアンワイン＆カフェレストラン" plus ドリア and ドリンクバー.
        assertEquals("Food & Dining", categoryOf("saizeriya.txt"))
    }

    @Test
    fun `a supermarket receipt is groceries`() {
        // The receipt advertises 食料品 and the store name carries スーパー wording.
        assertEquals("Groceries", categoryOf("ok_store.txt"))
    }

    @Test
    fun `a baby goods receipt is shopping`() {
        // "Baby & Kids" and バレーシューズ.
        assertEquals("Shopping", categoryOf("nishimatsuya.txt"))
    }

    @Test
    fun `a receipt with no recognisable words stays uncategorised`() {
        // Guessing here would file the expense under a category the receipt
        // never supported; leaving it unknown asks the user instead.
        val draft = parseReceiptText(
            """
            ACME
            2026/10/24
            Widget  ¥100
            合計  ¥100
            """.trimIndent()
        )
        assertEquals(CATEGORY_UNKNOWN, draft.category)
    }

    @Test
    fun `transport wording wins over an incidental food word`() {
        val draft = parseReceiptText(
            """
            JR東日本
            2026/10/24
            乗車券  ¥500
            定期券  ¥5,000
            合計  ¥5,500
            """.trimIndent()
        )
        assertEquals("Transport", draft.category)
    }
}
