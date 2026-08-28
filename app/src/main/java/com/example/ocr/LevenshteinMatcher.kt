package com.example.ocr

import com.example.data.model.ItemWithLocations
import com.example.data.model.PackageType
import java.util.Locale
import kotlin.math.min

object LevenshteinMatcher {

    // Regex to detect carton / bulk packaging keywords
    private val CARTON_REGEX = Regex("(?i)\\b(CTN|BOX|CS|CARTON|CASES?|PACKS?|CARTONS)\\b")

    // Regex to remove currency symbols and prices like "$12.99", "Rs. 50.00", "45.00", "€10", etc.
    private val PRICE_REGEX = Regex("(?i)(?:[\\$₹€£]|Rs\\.?|INR|USD)?\\s*\\d+(?:[.,]\\d{2})\\b")

    // Regex to remove quantities like "2x", "5 pcs", "10 units", "x3", "12 pk", "500g", "1kg", etc.
    private val QUANTITY_REGEX = Regex("(?i)\\b\\d+\\s*(?:x|pcs|pk|ctn|boxes|units|pc|box|ea)\\b|\\b\\d+x\\b|\\bx\\d+\\b")

    // Regex to clean up leading numbering like "1. ", "02 - ", etc.
    private val LINE_NUMBER_REGEX = Regex("^\\s*\\d+[.)\\-:]\\s*")

    /**
     * Standard Levenshtein edit distance calculation between two strings.
     */
    fun calculateDistance(s1: String, s2: String): Int {
        val a = s1.lowercase(Locale.ROOT).trim()
        val b = s2.lowercase(Locale.ROOT).trim()

        if (a == b) return 0
        if (a.isEmpty()) return b.length
        if (b.isEmpty()) return a.length

        val dp = Array(a.length + 1) { IntArray(b.length + 1) }

        for (i in 0..a.length) dp[i][0] = i
        for (j in 0..b.length) dp[0][j] = j

        for (i in 1..a.length) {
            for (j in 1..b.length) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                dp[i][j] = min(
                    dp[i - 1][j] + 1, // deletion
                    min(
                        dp[i][j - 1] + 1, // insertion
                        dp[i - 1][j - 1] + cost // substitution
                    )
                )
            }
        }
        return dp[a.length][b.length]
    }

    /**
     * Step 2: Extract packaging indicator from line string.
     * If (?i)\b(CTN|BOX|CS|CARTON)\b found, return CARTON; else LOOSE.
     */
    fun extractPackageType(rawLine: String): PackageType {
        return if (CARTON_REGEX.containsMatchIn(rawLine)) {
            PackageType.CARTON
        } else {
            PackageType.LOOSE
        }
    }

    /**
     * Step 2: Remove currency, prices, quantity, and punctuation to isolate raw product query.
     */
    fun cleanProductName(rawLine: String): String {
        var text = rawLine.trim()
        text = text.replace(LINE_NUMBER_REGEX, "")
        text = text.replace(PRICE_REGEX, "")
        text = text.replace(QUANTITY_REGEX, "")
        text = text.replace(CARTON_REGEX, "")
        // Clean out excessive punctuation while preserving alphanumeric characters
        text = text.replace(Regex("[#*~_|\\[\\]{}()<>]"), " ")
        text = text.replace(Regex("\\s+"), " ").trim()
        return text
    }

    data class MatchResult(
        val item: ItemWithLocations,
        val bestDistance: Int,
        val matchedTerm: String,
        val confidence: Float
    )

    /**
     * Step 3: Find the database item with the lowest edit distance (threshold <= 3 edits or token match).
     */
    fun findBestMatch(
        rawQuery: String,
        candidates: List<ItemWithLocations>,
        maxDistanceThreshold: Int = 3
    ): MatchResult? {
        val cleanQuery = cleanProductName(rawQuery).lowercase(Locale.ROOT)
        if (cleanQuery.length < 2) return null

        var bestMatch: ItemWithLocations? = null
        var minDistance = Int.MAX_VALUE
        var matchedTerm = ""

        for (candidate in candidates) {
            val terms = mutableListOf<String>()
            terms.add(candidate.item.itemName)

            if (candidate.item.aliases.isNotBlank()) {
                val aliasList = candidate.item.aliases.split(",").map { it.trim() }
                terms.addAll(aliasList)
            }

            for (term in terms) {
                if (term.isBlank()) continue
                val cleanTerm = term.lowercase(Locale.ROOT)

                // Exact or Substring match
                if (cleanQuery == cleanTerm) {
                    return MatchResult(
                        item = candidate,
                        bestDistance = 0,
                        matchedTerm = term,
                        confidence = 1.0f
                    )
                }

                if (cleanQuery.contains(cleanTerm) || cleanTerm.contains(cleanQuery)) {
                    val dist = 1
                    if (dist < minDistance) {
                        minDistance = dist
                        bestMatch = candidate
                        matchedTerm = term
                    }
                    continue
                }

                val dist = calculateDistance(cleanQuery, cleanTerm)
                if (dist < minDistance) {
                    minDistance = dist
                    bestMatch = candidate
                    matchedTerm = term
                }
            }
        }

        if (bestMatch != null && minDistance <= maxDistanceThreshold) {
            val maxLen = maxOf(cleanQuery.length, matchedTerm.length, 1)
            val confidence = ((maxLen - minDistance).toFloat() / maxLen.toFloat()).coerceIn(0.1f, 1.0f)
            return MatchResult(
                item = bestMatch,
                bestDistance = minDistance,
                matchedTerm = matchedTerm,
                confidence = confidence
            )
        }

        return null
    }
}
