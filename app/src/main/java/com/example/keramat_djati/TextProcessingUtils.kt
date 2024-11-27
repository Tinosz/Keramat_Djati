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
        return groupedTextBlocks.entries.sortedBy { it.key }
            .mapNotNull { entry ->
                val line = entry.value.joinToString(" ") { it.text }
                val parts = line.split("\\s+".toRegex()).takeLast(2) // Assuming the last two elements are price and quantity
                if (parts.size == 2) {
                    try {
                        val quantity = parts[0].toInt()  // Parse quantity as an integer
                        val price = parts[1].filter { it.isDigit() || it == ',' }.replace(',', '.').toDouble()
                        val name = line.removeSuffix(parts.joinToString(" "))
                        ReceiptItem(name, quantity, price, quantity * price)  // Total is calculated as quantity * price
                    } catch (e: NumberFormatException) {
                        null
                    }
                } else {
                    null
                }
            }
    }
}
