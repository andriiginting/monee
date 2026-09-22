import data.ocr.ReceiptDraft
import data.ocr.parseReceiptText
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Regression cover for receipts that Vision returns as *columns*: a run of item
 * names followed by a separate run of prices, rather than one `name … amount`
 * row per item. Both fixtures are verbatim Vision output for photographed
 * receipts, and the expectations are the figures printed on the paper.
 */
class ColumnReceiptTest {
    private fun parse(name: String): ReceiptDraft {
        val stream = checkNotNull(javaClass.getResourceAsStream("/$name")) {
            "missing test fixture $name"
        }
        return parseReceiptText(stream.bufferedReader().readText())
    }

    private val okStore by lazy { parse("ok_store.txt") }
    private val nishimatsuya by lazy { parse("nishimatsuya.txt") }

    @Test
    fun `reads the ok store total`() {
        assertEquals(1_003_00L, okStore.totalMinor)
    }

    @Test
    fun `pairs ok store items with their own prices`() {
        assertEquals(
            listOf("Fニクウマ！メンチカツ" to 249_00L, "Fウスゴロモフライドチキンセット" to 680_00L),
            okStore.lineItems.map { it.name to it.amountMinor },
        )
    }

    @Test
    fun `reads the ok store branch name`() {
        assertEquals("オーケー北赤羽店", okStore.location)
    }

    @Test
    fun `reads the ok store purchase date`() {
        assertEquals("2026年09月22日", okStore.date)
    }

    @Test
    fun `reads the nishimatsuya total from the column pairing`() {
        // The labelled-total scan alone lands on ¥87 (a tax subtotal); the total
        // has to come from the 合計 row of the name/amount zip.
        assertEquals(965_00L, nishimatsuya.totalMinor)
    }

    @Test
    fun `pairs nishimatsuya items with their own prices`() {
        assertEquals(
            listOf(449_00L, 429_00L),
            nishimatsuya.lineItems.map { it.amountMinor },
        )
    }

    @Test
    fun `skips the stylised logo when reading the store name`() {
        // OCR renders the 西松屋 storefront logo as the latin string "Calbe Lat",
        // which must not win over the CJK store name below it.
        assertEquals("西松店", nishimatsuya.location)
    }

    @Test
    fun `reads the nishimatsuya purchase date`() {
        assertEquals("2026年9月22日", nishimatsuya.date)
    }

    @Test
    fun `item prices never exceed the total`() {
        listOf(okStore, nishimatsuya).forEach { draft ->
            val sum = draft.lineItems.sumOf { it.amountMinor }
            val total = draft.totalMinor ?: 0L
            check(sum <= total) { "items sum to $sum which exceeds total $total" }
        }
    }
}
