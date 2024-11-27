package com.example.keramat_djati

object RegexUtils {
    private val itemLineRegex = """^(.+?)\s+(\d+)\s+(\d+)\s+(\d+)$""".toRegex()
    private val totalRegex = """Total:\s+(\d+)$""".toRegex()

    fun parseReceiptItems(text: String): List<BillItem> {
        return text.lines().mapNotNull { line ->
            itemLineRegex.find(line)?.let { matchResult ->
                BillItem(
                    description = matchResult.groupValues[1].trim(),
                    quantity = matchResult.groupValues[2].toInt(),
                    unitPrice = matchResult.groupValues[3].toDouble(),
                    subtotal = matchResult.groupValues[4].toDouble()
                )
            }
        }
    }

    fun parseTotal(text: String): Double {
        return text.lines().mapNotNull { line ->
            totalRegex.find(line)?.let { matchResult ->
                matchResult.groupValues[1].toDouble()
            }
        }.sum()
    }
}