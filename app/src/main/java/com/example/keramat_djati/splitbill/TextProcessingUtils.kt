package com.example.keramat_djati.splitbill

object TextProcessingUtils {

    // This function filters and cleans up the OCR blocks
    private fun filterRelevantTextBlocks(blocks: List<TextBlock>): List<TextBlock> {
        return blocks.filter { block ->
            // Remove empty blocks and blocks with only symbols or special characters
            block.text.isNotEmpty() && block.text.matches("^[a-zA-Z0-9\\s,.]+$".toRegex())
        }
    }

    // This function normalizes the text by removing non-alphanumeric characters and trimming spaces
    private fun normalizeText(blocks: List<TextBlock>): List<TextBlock> {
        return blocks.map { block ->
            val cleanText = block.text.trim().replace("[^\\w\\s]".toRegex(), "")  // Remove non-alphanumeric characters
            block.copy(text = cleanText)
        }
    }

    // This function sorts text blocks by vertical position (Y coordinate)
    private fun sortTextBlocksByVerticalPosition(blocks: List<TextBlock>): List<TextBlock> {
        return blocks.sortedBy { it.y }
    }

    // This function groups text blocks by horizontal spacing (blocks that are too far apart are not grouped together)
    private fun filterTextBlocksByHorizontalSpacing(blocks: List<TextBlock>, threshold: Int = 100): List<List<TextBlock>> {
        val groupedBlocks = mutableListOf<MutableList<TextBlock>>()
        var currentGroup = mutableListOf<TextBlock>()
        var lastX = blocks.first().x

        for (block in blocks) {
            if (block.x - lastX > threshold) {
                // If horizontal gap is large, start a new group
                groupedBlocks.add(currentGroup)
                currentGroup = mutableListOf()
            }
            currentGroup.add(block)
            lastX = block.x
        }

        if (currentGroup.isNotEmpty()) {
            groupedBlocks.add(currentGroup)
        }

        return groupedBlocks
    }

    // Format text blocks into receipt items (only item name and price)
    private fun formatTextBlocksToReceiptItems(groupedTextBlocks: List<List<TextBlock>>): List<ReceiptItem> {
        return groupedTextBlocks.mapNotNull { group ->
            val line = group.joinToString(" ") { it.text }

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

    // Main function to process the blocks
    fun processReceipt(blocks: List<TextBlock>): List<ReceiptItem> {
        // Step 1: Filter and clean the blocks
        val filteredBlocks = filterRelevantTextBlocks(blocks)

        // Step 2: Normalize the text
        val normalizedBlocks = normalizeText(filteredBlocks)

        // Step 3: Sort by vertical (Y) position
        val sortedBlocks = sortTextBlocksByVerticalPosition(normalizedBlocks)

        // Step 4: Group blocks by horizontal spacing
        val groupedBlocks = filterTextBlocksByHorizontalSpacing(sortedBlocks)

        // Step 5: Format the grouped blocks into receipt items (only name and price)
        return formatTextBlocksToReceiptItems(groupedBlocks)
    }
}
