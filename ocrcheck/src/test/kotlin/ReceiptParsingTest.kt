import data.ocr.formatReceiptAmount
import data.ocr.parseReceiptText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ReceiptParsingTest {

    private val japaneseConbini = """
        ローソン 渋谷店
        2024/03/15 14:23
        おにぎり 鮭            ¥150
        サンドイッチ           ¥398
        コーヒー               ¥110
        小計                   ¥658
        消費税(8%)             ¥52
        合計                   ¥710
        お預り                 ¥1,000
        お釣り                 ¥290
    """.trimIndent()

    private val usStyle = """
        WHOLE FOODS MARKET
        123 Main Street
        Mar 15, 2024
        Bananas               3.99
        Almond Milk           4.49
        Sourdough Bread       6.50
        Subtotal             14.98
        Tax                   1.24
        TOTAL                16.22
        VISA                 16.22
    """.trimIndent()

    @Test
    fun `picks the labelled total not the cash tendered`() {
        assertEquals(71000L, parseReceiptText(japaneseConbini).totalMinor)
    }

    @Test
    fun `keeps cents on decimal totals`() {
        assertEquals(1622L, parseReceiptText(usStyle).totalMinor)
    }

    @Test
    fun `does not treat a timestamp line as a purchase`() {
        val names = parseReceiptText(japaneseConbini).lineItems.map { it.name }
        assertTrue(names.none { it.contains("2024") }, "timestamp leaked into items: $names")
    }

    @Test
    fun `does not treat a date line as a purchase`() {
        val names = parseReceiptText(usStyle).lineItems.map { it.name }
        assertTrue(names.none { it.contains("Mar") }, "date leaked into items: $names")
    }

    @Test
    fun `filters japanese total and tax labels from items`() {
        val names = parseReceiptText(japaneseConbini).lineItems.map { it.name }
        listOf("小計", "合計", "消費税", "お預り", "お釣り").forEach { label ->
            assertTrue(names.none { it.contains(label) }, "'$label' leaked into items: $names")
        }
    }

    @Test
    fun `filters english total and payment labels from items`() {
        val names = parseReceiptText(usStyle).lineItems.map { it.name }
        listOf("Subtotal", "Tax", "TOTAL", "VISA").forEach { label ->
            assertTrue(names.none { it.equals(label, ignoreCase = true) }, "'$label' leaked: $names")
        }
    }

    @Test
    fun `extracts japanese items with correct amounts`() {
        val items = parseReceiptText(japaneseConbini).lineItems
        assertEquals(listOf("おにぎり 鮭", "サンドイッチ", "コーヒー"), items.map { it.name })
        assertEquals(listOf(15000L, 39800L, 11000L), items.map { it.amountMinor })
    }

    @Test
    fun `extracts us items with cents intact`() {
        val items = parseReceiptText(usStyle).lineItems
        assertEquals(listOf("Bananas", "Almond Milk", "Sourdough Bread"), items.map { it.name })
        assertEquals(listOf(399L, 449L, 650L), items.map { it.amountMinor })
    }

    @Test
    fun `ignores phone numbers`() {
        val receipt = """
            SUPER STORE
            TEL 03-1234-5678
            2024/03/15
            Milk 2 x 250          500
            Eggs                  320
            TOTAL                 820
        """.trimIndent()
        val draft = parseReceiptText(receipt)
        assertTrue(
            draft.lineItems.none { it.name.contains("TEL") },
            "phone leaked: ${draft.lineItems}",
        )
        assertEquals(82000L, draft.totalMinor)
    }

    @Test
    fun `strips quantity prefix from item names`() {
        val receipt = """
            SHOP
            Milk 2 x 250          500
            TOTAL                 500
        """.trimIndent()
        assertEquals("Milk", parseReceiptText(receipt).lineItems.single().name)
    }

    @Test
    fun `extracts date and location`() {
        val jp = parseReceiptText(japaneseConbini)
        assertEquals("2024/03/15", jp.date)
        assertEquals("ローソン 渋谷店", jp.location)

        val us = parseReceiptText(usStyle)
        assertEquals("Mar 15, 2024", us.date)
        assertEquals("WHOLE FOODS MARKET", us.location)
    }

    @Test
    fun `empty text yields an empty draft`() {
        val draft = parseReceiptText("")
        assertNull(draft.totalMinor)
        assertNull(draft.date)
        assertTrue(draft.lineItems.isEmpty())
    }

    @Test
    fun `formats amounts for display`() {
        assertEquals("—", formatReceiptAmount(null))
        assertEquals("¥0", formatReceiptAmount(0))
        assertEquals("¥710", formatReceiptAmount(71000))
        assertEquals("¥16.22", formatReceiptAmount(1622))
        assertEquals("¥1,000", formatReceiptAmount(100000))
        assertEquals("¥1,234,567", formatReceiptAmount(123456700))
        assertEquals("¥0.05", formatReceiptAmount(5))
        assertEquals("-¥2.50", formatReceiptAmount(-250))
    }

    @Test
    fun `parses european grouping`() {
        val receipt = """
            LADEN
            Brot                 1.234,56
            TOTAL                1.234,56
        """.trimIndent()
        assertEquals(123456L, parseReceiptText(receipt).totalMinor)
    }

    @Test
    fun `thousands separator is not read as decimals`() {
        val receipt = """
            SHOP
            TOTAL                1,500
        """.trimIndent()
        assertEquals(150000L, parseReceiptText(receipt).totalMinor)
    }

    @Test
    fun `ignores malformed amount captures`() {
        val receipt = """
            SHOP
            Weird item            12..34
            Trailing dot          99.
        """.trimIndent()
        assertTrue(
            parseReceiptText(receipt).lineItems.isEmpty(),
            "malformed amounts were accepted: ${parseReceiptText(receipt).lineItems}",
        )
    }

    @Test
    fun `handles three digit grouping without decimals`() {
        val receipt = """
            SHOP
            TOTAL             1.234.567
        """.trimIndent()
        assertEquals(123456700L, parseReceiptText(receipt).totalMinor)
    }

    @Test
    fun `falls back to largest amount when no total label`() {
        val receipt = """
            KIOSK
            Water                 120
            Snack                 250
        """.trimIndent()
        assertEquals(25000L, parseReceiptText(receipt).totalMinor)
    }
}
