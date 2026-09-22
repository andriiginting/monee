import data.ocr.parseReceiptText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Receipts print the purchase date in whatever convention the till uses. The
 * draft renders one format so the scan screen reads the same either way.
 */
class ReceiptDateFormatTest {
    private fun dateOf(line: String) = parseReceiptText("STORE\n$line\nItem  ¥100").date

    @Test
    fun `renders a japanese date`() {
        assertEquals("Sep 22, 2026", dateOf("2026年9月22日"))
    }

    @Test
    fun `renders a zero padded japanese date`() {
        assertEquals("Sep 22, 2026", dateOf("2026年09月22日"))
    }

    @Test
    fun `renders an iso date`() {
        assertEquals("Oct 24, 2026", dateOf("2026/10/24"))
    }

    @Test
    fun `renders a dotted iso date`() {
        assertEquals("Oct 24, 2026", dateOf("2026.10.24"))
    }

    @Test
    fun `renders a month name date already in the target shape`() {
        assertEquals("Oct 24, 2026", dateOf("Oct 24, 2026"))
    }

    @Test
    fun `renders a long month name`() {
        assertEquals("Oct 24, 2026", dateOf("October 24, 2026"))
    }

    @Test
    fun `reads an unambiguous day first date as a day`() {
        // 24 cannot be a month, so this is the 24th regardless of convention.
        assertEquals("Oct 24, 2026", dateOf("24/10/2026"))
    }

    @Test
    fun `reads an ambiguous slash date as month first`() {
        // 03/04 could be either; US ordering matches the named-month receipts.
        assertEquals("Mar 4, 2026", dateOf("03/04/2026"))
    }

    @Test
    fun `keeps a leap day`() {
        assertEquals("Feb 29, 2024", dateOf("2024年2月29日"))
    }

    @Test
    fun `leaves an impossible date unchanged rather than coercing it`() {
        // Feb 30 is not a date. Reporting it verbatim is better than inventing
        // a plausible-looking one the receipt never carried.
        assertEquals("2026.02.30", dateOf("2026.02.30"))
    }

    @Test
    fun `a receipt with no date still has none`() {
        assertNull(parseReceiptText("STORE\nItem  ¥100").date)
    }
}
