import data.ocr.parseReceiptText
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Pins the failure modes of the column pairing, which zips a run of item names
 * against the run of amounts that follows it. Every one of these mis-pairs
 * silently: the parse succeeds and reports confident but wrong prices.
 */
class ColumnPairingTest {
    @Test
    fun `store name adjacent to the items does not steal the first price`() {
        // The fixtures all happen to carry a phone number or date between the
        // header and the first item, which breaks the text run by accident. A
        // receipt without one must not pair the store name against a price.
        val draft = parseReceiptText(
            """
            オーケー北赤羽店
            商品A
            商品B
            合計
            ¥100
            ¥200
            ¥300
            """.trimIndent()
        )

        assertEquals(
            listOf("商品A" to 100_00L, "商品B" to 200_00L),
            draft.lineItems.map { it.name to it.amountMinor },
        )
        assertEquals(300_00L, draft.totalMinor)
    }

    @Test
    fun `a header broken by a phone line parses the same way`() {
        val draft = parseReceiptText(
            """
            オーケー北赤羽店
            03-5918-6605
            商品A
            商品B
            合計
            ¥100
            ¥200
            ¥300
            """.trimIndent()
        )

        assertEquals(
            listOf("商品A" to 100_00L, "商品B" to 200_00L),
            draft.lineItems.map { it.name to it.amountMinor },
        )
        assertEquals(300_00L, draft.totalMinor)
    }

    @Test
    fun `picks the grand total when a run carries more than one total row`() {
        // Receipts print a 合計 per tax bracket before the grand total, and the
        // rows do not arrive in a guaranteed order.
        val draft = parseReceiptText(
            """
            西松屋本店
            03-1111-2222
            商品A
            商品B
            合計
            商品C
            合計
            ¥100
            ¥200
            ¥300
            ¥400
            ¥50
            """.trimIndent()
        )

        assertEquals(300_00L, draft.totalMinor)
    }

    @Test
    fun `a stray column does not override a total read from inline rows`() {
        // Items here come from the inline layout; the trailing block is loyalty
        // metadata, not a price column, and must not supply the total.
        val draft = parseReceiptText(
            """
            MY STORE
            Apple      ¥100
            Bread      ¥200
            合計       ¥300
            ポイント
            有効期限
            ¥9999
            ¥8888
            """.trimIndent()
        )

        assertEquals(300_00L, draft.totalMinor)
    }

    @Test
    fun `discount rows are not paired as purchased items`() {
        val draft = parseReceiptText(
            """
            スーパー本店
            03-1111-2222
            商品A
            値引
            合計
            ¥500
            ¥-100
            ¥400
            """.trimIndent()
        )

        assertEquals(listOf("商品A" to 500_00L), draft.lineItems.map { it.name to it.amountMinor })
        assertEquals(400_00L, draft.totalMinor)
    }

    @Test
    fun `items are found when the date is printed at the foot of the receipt`() {
        // The masthead guard keys off the date, but plenty of receipts print it
        // below the totals. Treating that as the end of the header rejected the
        // entire body and returned a draft carrying nothing but the date.
        val draft = parseReceiptText(
            """
            オーケー北赤羽店
            商品A
            商品B
            合計
            ¥100
            ¥200
            ¥300
            2026年9月22日
            """.trimIndent()
        )

        assertEquals(
            listOf("商品A" to 100_00L, "商品B" to 200_00L),
            draft.lineItems.map { it.name to it.amountMinor },
        )
        assertEquals(300_00L, draft.totalMinor)
    }

    @Test
    fun `items are found on a receipt with no date at all`() {
        val draft = parseReceiptText(
            """
            オーケー北赤羽店
            商品A
            商品B
            合計
            ¥100
            ¥200
            ¥300
            """.trimIndent()
        )

        assertEquals(2, draft.lineItems.size)
        assertEquals(300_00L, draft.totalMinor)
    }
}
