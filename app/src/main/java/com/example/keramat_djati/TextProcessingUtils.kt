package com.example.keramat_djati

import android.util.Log

object TextProcessingUtils {

    // This function groups and formats the blocks into a list of receipt items
    fun filterAndGroupTextBlocks(blocks: List<TextBlock>): Map<Int, List<TextBlock>> {
        if (blocks.isEmpty()) return emptyMap()

        // Group text blocks by their Y coordinate, making sure items appear together vertically
        val lines = blocks.groupBy { it.y / 10 }  // Adjust the threshold (20) if needed

        val refinedGroups = mutableMapOf<Int, MutableList<TextBlock>>()
        lines.entries.forEach { (key, textBlocks) ->
            val sortedBlocks = textBlocks.sortedBy { it.x }  // Sort by horizontal X to maintain item order

            val dynamicGroups = mutableListOf<List<TextBlock>>()
            var currentGroup = mutableListOf<TextBlock>()
            var lastX = sortedBlocks.first().x

            // Group text blocks horizontally that are close enough
            sortedBlocks.forEach { block ->
                if ((block.x - lastX) > calculateImprovedThreshold(sortedBlocks)) {
                    dynamicGroups.add(currentGroup)
                    currentGroup = mutableListOf()
                }
                currentGroup.add(block)
                lastX = block.x
            }

            if (currentGroup.isNotEmpty()) dynamicGroups.add(currentGroup)

            // Store the groups by Y position
            refinedGroups[key] = dynamicGroups.flatten().toMutableList()
        }

        return refinedGroups
    }

    private fun calculateImprovedThreshold(blocks: List<TextBlock>): Int {
        val distances = blocks.zipWithNext { a, b -> b.x - a.x }
        val averageDistance = if (distances.isNotEmpty()) distances.average() else 100.0
        return (averageDistance * 1.2).toInt() // Adjust the factor as necessary based on data
    }


    fun formatTextBlocksToReceiptItems(groupedTextBlocks: Map<Int, List<TextBlock>>): List<ReceiptItem> {
        return groupedTextBlocks.entries.sortedBy { it.key }
            .mapNotNull { entry ->
                val line = entry.value.joinToString(" ") { it.text }

                // Split the line into parts, expecting price to be the last part
                val parts = line.split("\\s+".toRegex())

                // Ensure there's at least one part for the name and another for the price
                if (parts.isNotEmpty()) {
                    try {
                        // Extract item name (everything except the last part)
                        val name = parts.dropLast(1).joinToString(" ")
                        // Extract the price (last part)
                        val priceStr = parts.last().filter { it.isDigit() || it == '.' }  // Clean price string (e.g., remove currency symbols)
                        val price = priceStr.toDouble()

                        // Create the ReceiptItem with name and price
                        ReceiptItem(itemName = name, price = price, total = price)  // Since no quantity, total = price
                    } catch (e: NumberFormatException) {
                        // If the price parsing fails, ignore this entry
                        null
                    }
                } else {
                    null
                }
            }
    }

}