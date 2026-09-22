package data.ocr

/**
 * Amounts are stored in **minor units** (sen for JPY-formatted output, cents for
 * decimal currencies). Receipts routinely print decimals, and truncating them to
 * whole units silently loses money, so parsing keeps the fractional part.
 */
private const val MINOR_UNITS_PER_MAJOR = 100L

data class ReceiptLineItem(
    val name: String,
    val amountMinor: Long,
)

data class ReceiptDraft(
    val totalMinor: Long?,
    val date: String?,
    val location: String?,
    val lineItems: List<ReceiptLineItem>,
    val account: String = "Cash Wallet",
    val category: String = "Select Category...",
)

/**
 * CJK receipt keywords cannot be anchored with `\b`: word boundaries are defined
 * between ASCII word and non-word characters, so `\b合計\b` never matches. These
 * bound a keyword by "not a letter or digit" instead, which works in both scripts.
 */
private const val L = """(?<![\p{L}\p{N}])"""
private const val R = """(?![\p{L}\p{N}])"""

/**
 * OCR engines do not preserve the visual layout of a receipt. Vision (iOS) in
 * particular emits text in reading-order blocks, so a two-column receipt arrives
 * as a run of item names followed by a separate run of prices:
 *
 *     ガーリックフォッカチオ     <- names, no amounts
 *     ポテトのグリル
 *     ¥200                      <- amounts, no names
 *     ¥300
 *
 * ML Kit (Android) more often keeps "name … amount" on one line. Both layouts
 * have to work, so parsing runs in two passes: same-line pairs first, and a
 * column pass that zips a run of names against the run of amounts that follows.
 */

/**
 * A line that is nothing but a money amount (its own OCR block). A currency mark
 * or a grouped/decimal figure is required: a bare small integer on its own line is
 * far more often a count (party size, quantity, register no) than a price.
 */
private val amountOnlyLine = Regex("""^\s*[(（]?\s*[¥￥$]\s*(-?[0-9][0-9.,]*)\s*[)）]?\s*[-*]?\s*$""")

/** Bare figure with grouping/decimals, e.g. "1,234" or "12.50" on its own line. */
private val bareGroupedAmountLine = Regex("""^\s*[(（]?\s*(-?[0-9]{1,3}(?:[.,][0-9]{3})+(?:[.,][0-9]{1,2})?|-?[0-9]+[.,][0-9]{2})\s*[)）]?\s*$""")

/** "name ... ¥1,234" on a single line. */
private val amountAtEnd = Regex("""(?:[¥￥$])\s*(-?[0-9][0-9.,]*)\s*[-*]?\s*$""")

/** Same, but tolerating a missing currency mark (ASCII receipts). */
private val looseAmountAtEnd = Regex("""(?:[¥￥$])?\s{2,}(-?[0-9][0-9.,]*)\s*[-*]?\s*$""")

private val datePattern = Regex(
    """(?i)(?:20\d{2}[-/.]\d{1,2}[-/.]\d{1,2}|\d{1,2}[-/.]\d{1,2}[-/.]20\d{2}|(?:jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)[a-z]*\.?\s+\d{1,2},?\s+20\d{2}|20\d{2}年\d{1,2}月\d{1,2}日)"""
)

/**
 * Everything from the credit-slip header onwards is card-processing metadata:
 * terminal ids, approval codes, AIDs, masked card numbers. Those numbers are not
 * money, and left in they dominate the "largest amount" fallback.
 */
private val paymentSlipStart = Regex(
    """(?i)(?:クレジット売上票|売上票|加盟店名|端末番号|ご利用日|会員番号|承認番号|処理通番|カード会社|card\s+company|app\s+code|tran\s+no|term\s+no|merchant\s+copy|customer\s+copy)"""
)

/** Identifier-ish lines that must never be read as a price. */
private val identifierLine = Regex(
    """(?i)(?:tel|phone|fax|電話|レジ|登録番号|伝票番号|aid|acct|visa|master|jcb|amex|ic\b|no\.|№)"""
)

/** Bare digit runs (register no, table no, card tails) and separator rules. */
private val bareIdentifier = Regex("""^\s*[0-9]{3,}\s*$""")
private val separatorRun = Regex("""^[\s\-=_.·*]+$""")
private val maskedNumber = Regex("""[*×]{3,}|\d{4,}-\d{3,}|\d{6,}""")

private val totalLabels = Regex("""$L(?:合計|総計|お買上計|お買上げ計|(?i:grand\s+total|total|amount\s+due|balance\s+due))$R""")

/** Looks like a total but is not the amount charged. */
private val nonTotalLabels = Regex(
    """$L(?:小計|お預り|お預かり|お釣り|おつり|釣銭|現金|対象計|内税額|外税額|税額|(?i:subtotal|sub\s+total|change|cash|tendered))$R"""
)

private val taxLabels = Regex("""$L(?:消費税|内税|外税|(?i:tax|vat|gst))$R""")

private val ignoredItemLabels = Regex(
    """$L(?:小計|合計|総計|消費税|内税|外税|税額|対象計|内税額|外税額|お預り|お預かり|お釣り|おつり|釣銭|現金|クレジット|ポイント|人数|(?i:subtotal|sub\s+total|total|tax|vat|gst|change|cash|tendered|credit|debit|card|visa|mastercard|amex|discount|amount\s+due|balance\s+due|points?|qty))$R"""
)

/** Leading item/PLU code OCR prints before the name ("04333ガーリック…"). */
private val leadingItemCode = Regex("""^[(（]?\s*\d{4,6}\s*[)）]?\s*""")

/** Trailing quantity/unit-price noise ("@200 x 2コ", "2 x 250"). */
private val quantityNoise = Regex("""(?i)[@＠]?\s*\d+\s*(?:x|×|@|コ|個)\s*[\d.,]*\s*$""")

fun parseReceiptText(text: String): ReceiptDraft {
    val allLines = text
        .lineSequence()
        .map(String::trim)
        .filter(String::isNotBlank)
        .filterNot { separatorRun.matches(it) }
        .toList()

    // Drop the card-processing slip; it contributes no prices, only large ids.
    val slipIndex = allLines.indexOfFirst { paymentSlipStart.containsMatchIn(it) }
    val lines = if (slipIndex > 0) allLines.subList(0, slipIndex) else allLines

    val date = allLines.firstNotNullOfOrNull { datePattern.find(it)?.value }

    /** True when a line can never carry a monetary amount. */
    fun isNoise(line: String): Boolean =
        identifierLine.containsMatchIn(line) ||
            maskedNumber.containsMatchIn(line) ||
            bareIdentifier.matches(line) ||
            datePattern.containsMatchIn(line)

    fun amountOnly(line: String): Long? {
        if (isNoise(line)) return null
        val m = amountOnlyLine.find(line) ?: bareGroupedAmountLine.find(line) ?: return null
        return m.groupValues[1].toMinorUnits()
    }

    fun inlineAmount(line: String): Long? {
        if (isNoise(line)) return null
        val m = amountAtEnd.find(line) ?: looseAmountAtEnd.find(line) ?: return null
        return m.groupValues[1].toMinorUnits()
    }

    // --- total -------------------------------------------------------------
    // A labelled total may carry its amount inline, or sit in a label column
    // whose amounts follow further down. Resolve both.
    fun labelledTotal(): Long? {
        val labelIdx = lines.indexOfLast {
            totalLabels.containsMatchIn(it) &&
                !nonTotalLabels.containsMatchIn(it) &&
                !taxLabels.containsMatchIn(it)
        }
        if (labelIdx < 0) return null

        inlineAmount(lines[labelIdx])?.let { return it }

        // Column layout: count how many label lines precede this one in the
        // current run, then take the amount at the same offset in the run of
        // amount-only lines that follows.
        var runStart = labelIdx
        while (runStart > 0 &&
            amountOnly(lines[runStart - 1]) == null &&
            inlineAmount(lines[runStart - 1]) == null &&
            lines[runStart - 1].any { it.isLetter() }
        ) runStart--

        val offset = labelIdx - runStart
        val amounts = lines
            .drop(labelIdx + 1)
            .dropWhile { amountOnly(it) == null }
            .takeWhile { amountOnly(it) != null }
            .mapNotNull(::amountOnly)

        return amounts.getOrNull(offset)
    }

    val total = labelledTotal() ?: lines
        .asSequence()
        .filterNot { nonTotalLabels.containsMatchIn(it) }
        .mapNotNull { inlineAmount(it) ?: amountOnly(it) }
        .maxOrNull()

    // --- location ----------------------------------------------------------
    // The store name is the first substantial line that is not an amount, a date
    // or an identifier. A long *Japanese* line near the top is usually a tagline
    // ("イタリアンワイン＆カフェレストラン"), so CJK candidates are length-capped;
    // latin names ("WHOLE FOODS MARKET") routinely run longer and are not.
    val location = lines
        .take(6)
        .firstOrNull { line ->
            val cjk = line.any { it.code in 0x3000..0x9FFF || it.code in 0xFF00..0xFFEF }
            !isNoise(line) &&
                amountOnly(line) == null &&
                inlineAmount(line) == null &&
                line.length >= 2 &&
                (!cjk || line.length <= 14) &&
                line.any { it.isLetter() } &&
                !ignoredItemLabels.containsMatchIn(line)
        }

    // --- line items --------------------------------------------------------
    fun cleanName(raw: String): String =
        raw.replace(leadingItemCode, "")
            .replace(quantityNoise, "")
            .trim()
            .trimEnd('-', ':', '*', '@', '・')
            .trim()

    fun isItemName(line: String): Boolean =
        !isNoise(line) &&
            amountOnly(line) == null &&
            inlineAmount(line) == null &&
            !ignoredItemLabels.containsMatchIn(line) &&
            line.any { it.isLetter() } &&
            cleanName(line).length >= 2

    val inlineItems = lines.mapNotNull { line ->
        if (ignoredItemLabels.containsMatchIn(line) || isNoise(line)) return@mapNotNull null
        val match = amountAtEnd.find(line) ?: looseAmountAtEnd.find(line) ?: return@mapNotNull null
        val amount = match.groupValues[1].toMinorUnits() ?: return@mapNotNull null
        val name = cleanName(line.removeRange(match.range))
        if (name.length < 2) null else ReceiptLineItem(name, amount)
    }

    // Column layout: a run of item names immediately followed by a run of
    // amount-only lines, paired by position.
    val columnItems = buildList {
        var i = 0
        while (i < lines.size) {
            if (!isItemName(lines[i])) { i++; continue }

            val names = mutableListOf<String>()
            while (i < lines.size && isItemName(lines[i])) {
                names += lines[i]
                i++
            }

            // Skip stray non-amount lines (e.g. "人数", "3") between the columns.
            var j = i
            while (j < lines.size && amountOnly(lines[j]) == null && j - i < 3) j++

            val amounts = mutableListOf<Long>()
            while (j < lines.size && amountOnly(lines[j]) != null) {
                amounts += amountOnly(lines[j])!!
                j++
            }

            if (names.size >= 2 && amounts.size >= 2) {
                names.zip(amounts).forEach { (n, a) -> add(ReceiptLineItem(cleanName(n), a)) }
                i = j
            }
        }
    }

    val lineItems = if (inlineItems.size >= columnItems.size) inlineItems else columnItems

    return ReceiptDraft(
        totalMinor = total,
        date = date,
        location = location,
        lineItems = lineItems,
    )
}

/**
 * Parses a printed amount into minor units without losing the fractional part.
 * Handles both `1,234.56` and the European `1.234,56` grouping.
 */
private fun String.toMinorUnits(): Long? {
    val raw = trim()
    if (raw.isEmpty()) return null

    val negative = raw.startsWith('-')
    val digitsOnly = raw.removePrefix("-")

    if (digitsOnly.isEmpty()) return null
    if (!digitsOnly.first().isDigit() || !digitsOnly.last().isDigit()) return null
    if (digitsOnly.any { !it.isDigit() && it != '.' && it != ',' }) return null
    if (Regex("""[.,]{2,}""").containsMatchIn(digitsOnly)) return null

    val lastDot = digitsOnly.lastIndexOf('.')
    val lastComma = digitsOnly.lastIndexOf(',')
    val decimalIndex = maxOf(lastDot, lastComma)

    val fractionLength = if (decimalIndex >= 0) digitsOnly.length - decimalIndex - 1 else 0
    val isDecimalSeparator = decimalIndex >= 0 &&
        fractionLength in 1..2 &&
        digitsOnly.count { it == digitsOnly[decimalIndex] } == 1

    val majorPart: String
    val minorPart: String
    if (isDecimalSeparator) {
        majorPart = digitsOnly.substring(0, decimalIndex).filter(Char::isDigit)
        minorPart = digitsOnly.substring(decimalIndex + 1).filter(Char::isDigit)
    } else {
        majorPart = digitsOnly.filter(Char::isDigit)
        minorPart = ""
    }

    if (majorPart.isEmpty() && minorPart.isEmpty()) return null

    val major = (majorPart.ifEmpty { "0" }).toLongOrNull() ?: return null
    val minor = when (minorPart.length) {
        0 -> 0L
        1 -> (minorPart.toLongOrNull() ?: return null) * 10
        else -> minorPart.toLongOrNull() ?: return null
    }

    val value = major * MINOR_UNITS_PER_MAJOR + minor
    return if (negative) -value else value
}

/**
 * Formats minor units for display. Whole amounts render without a fractional part
 * (matching how JPY receipts read); amounts with a remainder keep two decimals.
 */
fun formatReceiptAmount(amountMinor: Long?): String {
    if (amountMinor == null) return "—"

    val negative = amountMinor < 0
    val absolute = if (negative) -amountMinor else amountMinor
    val major = absolute / MINOR_UNITS_PER_MAJOR
    val minor = absolute % MINOR_UNITS_PER_MAJOR

    val grouped = major
        .toString()
        .reversed()
        .chunked(3)
        .joinToString(",")
        .reversed()

    val body = if (minor == 0L) grouped else "$grouped.${minor.toString().padStart(2, '0')}"
    return if (negative) "-¥$body" else "¥$body"
}
