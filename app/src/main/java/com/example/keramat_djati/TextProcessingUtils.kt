package com.example.keramat_djati

data class TextBlock(
    val text: String,
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int
)

object TextProcessingUtils {
    fun filterAndGroupTextBlocks(blocks: List<TextBlock>): Map<Int, List<TextBlock>> {
        if (blocks.isEmpty()) return emptyMap()

        val sortedBlocks = blocks.sortedBy { it.y }
        val averageDistance = calculateAverageDistance(sortedBlocks)
        val threshold = averageDistance * 0.5  // Adjust this factor based on testing

        val groupedBlocks = mutableMapOf<Int, MutableList<TextBlock>>()
        var currentGroupKey = sortedBlocks.first().y / 10
        var currentGroup = mutableListOf<TextBlock>()

        sortedBlocks.forEach { block ->
            val blockKey = block.y / 10
            if (blockKey == currentGroupKey || block.y - currentGroup.last().y < threshold) {
                currentGroup.add(block)
            } else {
                if (currentGroup.isNotEmpty()) {
                    groupedBlocks[currentGroupKey] = currentGroup
                }
                currentGroupKey = blockKey
                currentGroup = mutableListOf(block)
            }
        }

        if (currentGroup.isNotEmpty()) {
            groupedBlocks[currentGroupKey] = currentGroup
        }

        return groupedBlocks
    }

    private fun calculateAverageDistance(blocks: List<TextBlock>): Double {
        var totalDistance = 0.0
        for (i in 1 until blocks.size) {
            totalDistance += (blocks[i].y - blocks[i - 1].y)
        }
        return if (blocks.size > 1) totalDistance / (blocks.size - 1) else 0.0
    }
}
