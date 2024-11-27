package com.example.keramat_djati

import android.util.Log

object TextProcessingUtils {

    // This function groups and formats the blocks into a list of receipt items
    fun filterAndGroupTextBlocks(blocks: List<TextBlock>): Map<Int, List<TextBlock>> {
        if (blocks.isEmpty()) return emptyMap()

        val lines = blocks.groupBy { it.y / 10 }

        val refinedGroups = mutableMapOf<Int, MutableList<TextBlock>>()
        lines.entries.forEach { (key, textBlocks) ->
            val sortedBlocks = textBlocks.sortedBy { it.x }
            val dynamicGroups = mutableListOf<List<TextBlock>>()
            var currentGroup = mutableListOf<TextBlock>()

            var lastX = sortedBlocks.first().x
            sortedBlocks.forEach { block ->
                if ((block.x - lastX) > calculateImprovedThreshold(sortedBlocks)) {
                    dynamicGroups.add(currentGroup)
                    currentGroup = mutableListOf()
                }
                currentGroup.add(block)
                lastX = block.x
            }
            if (currentGroup.isNotEmpty()) dynamicGroups.add(currentGroup)

            refinedGroups[key] = dynamicGroups.flatten().toMutableList()
        }

        return refinedGroups
    }

    private fun calculateImprovedThreshold(blocks: List<TextBlock>): Int {
        val distances = blocks.zipWithNext { a, b -> b.x - a.x }
        val averageDistance = if (distances.isNotEmpty()) distances.average() else 100.0
        return (averageDistance * 1.2).toInt() // Adjust this factor based on actual data
    }

    // Format text blocks into receipt items
    fun formatTextBlocksToReceiptItems(groupedTextBlocks: Map<Int, List<TextBlock>>): List<ReceiptItem> {
        val receiptItems = groupedTextBlocks.entries.sortedBy { it.key }
            .mapNotNull { entry ->
                val line = entry.value.joinToString(" ") { it.text }
                val parts = line.split("\\s+".toRegex()).filter { it.isNotEmpty() }

                if (parts.size >= 3) {
                    try {
                        val total = parts.last().filter { it.isDigit() || it == ',' || it == '.' }
                            .replace(',', '.')
                        val price = parts[parts.size - 2].filter { it.isDigit() || it == ',' || it == '.' }
                            .replace(',', '.')
                        val name = parts.dropLast(2).joinToString(" ")

                        val priceValue = price.toDoubleOrNull()
                        val totalValue = total.toDoubleOrNull()

                        if (priceValue != null && totalValue != null) {
                            val quantity = parts.getOrNull(parts.size - 3)?.toIntOrNull() ?: 1

                            return@mapNotNull ReceiptItem(name, quantity, priceValue, totalValue)
                        }
                    } catch (e: Exception) {
                        null
                    }
                }
                return@mapNotNull null
            }

        Log.d("Receipt Parsing", "Parsed Receipt Items: $receiptItems") // Log the result
        return receiptItems
    }

}
