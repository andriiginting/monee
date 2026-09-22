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
 * helpers bound a keyword by "not an ASCII letter/digit" instead, which works for
 * both scripts.
 */
private const val L = """(?<![\p{L}\p{N}])"""
private const val R = """(?![\p{L}\p{N}])"""

private val amountAtEnd = Regex(
    """(?:[¥￥$])?\s*(-?[0-9][0-9.,]*)\s*[-*]?\s*$"""
)

private val datePattern = Regex(
    """(?i)(?:20\d{2}[-/.]\d{1,2}[-/.]\d{1,2}|\d{1,2}[-/.]\d{1,2}[-/.]20\d{2}|(?:jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)[a-z]*\.?\s+\d{1,2},?\s+20\d{2}|20\d{2}年\d{1,2}月\d{1,2}日)"""
)

/** A line that is *primarily* a date/time stamp is never a purchased item. */
private val dateLikeLine = Regex(
    """^\s*(?:[¥￥$]?\s*)?(?:20\d{2}[-/.]\d{1,2}[-/.]\d{1,2}|\d{1,2}[-/.]\d{1,2}[-/.]20\d{2}|(?i:jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)[a-z]*\.?\s+\d{1,2},?\s+20\d{2}|20\d{2}年\d{1,2}月\d{1,2}日)[\s,]*(?:\d{1,2}[:：]\d{2}(?:[:：]\d{2})?)?\s*$"""
)

/** Phone / register / card-tail numbers read as "name + amount" without this guard. */
private val phoneLikeLine = Regex(
    """(?i)(?:tel|phone|fax|電話)|(?:\b\d{2,4}-\d{2,4}-\d{3,4}\b)|(?:[*x]{2,}\d{3,4}\s*$)"""
)

private val totalLabels = Regex("""(?i)$L(?:grand\s+total|total|amount\s+due|balance\s+due|合計|総計|お買上計)$R""")

/** Labels that look like a total but are not the amount actually charged. */
private val nonTotalLabels = Regex(
    """(?i)$L(?:subtotal|sub\s+total|change|cash|tendered|payment|card|visa|mastercard|amex|小計|お預り|お預かり|お釣り|おつり|釣銭|現金|クレジット)$R"""
)

private val taxLabels = Regex("""(?i)$L(?:tax|vat|gst|消費税|内税|外税)$R""")

private val ignoredItemLabels = Regex(
    """(?i)$L(?:subtotal|sub\s+total|total|tax|vat|gst|change|cash|tendered|credit|debit|card|visa|mastercard|amex|discount|amount\s+due|balance\s+due|points?|小計|合計|総計|消費税|内税|外税|お預り|お預かり|お釣り|おつり|釣銭|現金|クレジット|ポイント)$R"""
)

/** Strips a leading quantity prefix ("2 x 250", "3 @ 1.50") from an item name. */
private val quantityPrefix = Regex("""(?i)\s*\b\d+\s*(?:x|×|@)\s*[\d.,]+\s*$""")

fun parseReceiptText(text: String): ReceiptDraft {
    val lines = text
        .lineSequence()
        .map(String::trim)
        .filter(String::isNotBlank)
        .toList()

    /** A line is a candidate amount only when it is not a date/phone artifact. */
    fun amountOf(line: String): Long? {
        if (dateLikeLine.containsMatchIn(line)) return null
        if (phoneLikeLine.containsMatchIn(line)) return null
        return amountAtEnd.find(line)?.groupValues?.getOrNull(1)?.toMinorUnits()
    }

    // Prefer an explicitly labelled total, ignoring lines that merely *look* like
    // one (subtotal, cash tendered, change, card echo). Fall back to the largest
    // plausible amount only when nothing is labelled.
    val labelledTotal = lines
        .asSequence()
        .filter { totalLabels.containsMatchIn(it) }
        .filterNot { nonTotalLabels.containsMatchIn(it) }
        .filterNot { taxLabels.containsMatchIn(it) }
        .mapNotNull(::amountOf)
        .lastOrNull()

    val total = labelledTotal ?: lines
        .asSequence()
        .filterNot { nonTotalLabels.containsMatchIn(it) }
        .mapNotNull(::amountOf)
        .maxOrNull()

    val date = lines.firstNotNullOfOrNull { datePattern.find(it)?.value }

    val location = lines.firstOrNull { line ->
        !amountAtEnd.containsMatchIn(line) &&
            !datePattern.containsMatchIn(line) &&
            !phoneLikeLine.containsMatchIn(line) &&
            line.length >= 3
    }

    val lineItems = lines.mapNotNull { line ->
        if (ignoredItemLabels.containsMatchIn(line)) return@mapNotNull null
        if (dateLikeLine.containsMatchIn(line)) return@mapNotNull null
        if (phoneLikeLine.containsMatchIn(line)) return@mapNotNull null

        val amountMatch = amountAtEnd.find(line) ?: return@mapNotNull null
        val amount = amountMatch.groupValues[1].toMinorUnits() ?: return@mapNotNull null

        val name = line
            .removeRange(amountMatch.range)
            .trim()
            .trimEnd('-', ':', '*', '@')
            .replace(quantityPrefix, "")
            .trim()

        if (name.length < 2) null else ReceiptLineItem(name = name, amountMinor = amount)
    }

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

    // Reject captures that are not a well-formed number (trailing separator,
    // doubled separators) rather than silently reinterpreting them.
    if (digitsOnly.isEmpty()) return null
    if (!digitsOnly.first().isDigit() || !digitsOnly.last().isDigit()) return null
    if (digitsOnly.any { !it.isDigit() && it != '.' && it != ',' }) return null
    if (Regex("""[.,]{2,}""").containsMatchIn(digitsOnly)) return null

    // The final separator is a decimal point only when it is followed by 1-2 digits
    // and is the sole remaining separator of its kind.
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
