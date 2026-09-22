import data.ocr.parseReceiptText
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Regression fixture captured from a real photo (IMG_7603.heic) through the same
 * Vision text recogniser the iOS app uses. Vision emits reading-order blocks, so
 * this receipt arrives as a column of item names followed by a column of prices —
 * the layout that the original single-line parser could not read at all.
 */
class RealReceiptTest {

    private val draft by lazy {
        parseReceiptText(java.io.File(System.getProperty("saizeriya.path")!!).readText())
    }

    @Test
    fun `reads the charged total`() {
        assertEquals(270000L, draft.totalMinor)
    }

    @Test
    fun `reads the purchase date`() {
        assertEquals("2026/09/22", draft.date)
    }

    @Test
    fun `reads the store name not the tagline`() {
        assertEquals("サイゼリヤ", draft.location)
    }

    @Test
    fun `pairs every item with its own price`() {
        assertEquals(
            listOf(20000L, 30000L, 35000L, 50000L, 65000L, 30000L, 40000L),
            draft.lineItems.map { it.amountMinor },
        )
    }

    @Test
    fun `item prices sum to the total`() {
        assertEquals(draft.totalMinor, draft.lineItems.sumOf { it.amountMinor })
    }

    @Test
    fun `strips leading plu codes from item names`() {
        assertEquals("ガーリックフォッカチオ", draft.lineItems.first().name)
    }

    @Test
    fun `keeps card processing ids out of the result`() {
        val amounts = draft.lineItems.map { it.amountMinor } + listOfNotNull(draft.totalMinor)
        // 0719970 (tran no) and 71111-620-20497 (terminal) previously leaked in.
        assertEquals(emptyList(), amounts.filter { it > 1_000_000L })
    }
}
