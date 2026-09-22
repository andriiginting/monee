import data.ocr.parseReceiptText
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * OCR returns presentation variants that are not differences in the receipt:
 * full-width digits, spaces inside an amount, and item rows separated by a
 * single space rather than column whitespace.
 */
class OcrNormalizationTest {
    @Test
    fun `reads full width digits and amounts broken by spaces`() {
        val draft = parseReceiptText(
            """
            オーケー北赤羽店
            2026年9月22日
            商品A
            商品B
            合計
            ￥１ ０００
            ￥２００
            ￥１ ２００
            """.trimIndent()
        )

        assertEquals(
            listOf("商品A" to 1_000_00L, "商品B" to 200_00L),
            draft.lineItems.map { it.name to it.amountMinor },
        )
        assertEquals(1_200_00L, draft.totalMinor)
    }

    @Test
    fun `counts printed like item rows are not read as purchases`() {
        // "Guests 3" has the same shape as "Apple Pie 850". Reading the count as
        // money invents a purchase and shifts the column pairing.
        val draft = parseReceiptText(
            """
            MY STORE
            2026/09/22
            Table 41
            Guests 3
            Order 6356
            Apple Pie 850
            合計 850
            """.trimIndent()
        )

        assertEquals(listOf("Apple Pie" to 850_00L), draft.lineItems.map { it.name to it.amountMinor })
        assertEquals(850_00L, draft.totalMinor)
    }
}
