package com.hanyajasa.blockblast

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlin.random.Random

data class Position(val row: Int, val col: Int)

data class BlockShape(
    val points: List<Position>,
    val color: Long // Hex color
)

class GameViewModel : ViewModel() {
    val gridSize = 8
    var grid by mutableStateOf(Array(gridSize) { LongArray(gridSize) { 0L } })
    var score by mutableStateOf(0)
    var availableBlocks by mutableStateOf(listOf<BlockShape>())
    
    // UI State
    var selectedBlockIndex by mutableStateOf(-1) 
    var cursorPosition by mutableStateOf(Position(4, 4))
    var isPlacingBlock by mutableStateOf(false)

    private val shapes = listOf(
        BlockShape(listOf(Position(0,0)), 0xFF42A5F5), // 1x1
        BlockShape(listOf(Position(0,0), Position(0,1)), 0xFF66BB6A), // 1x2
        BlockShape(listOf(Position(0,0), Position(1,0)), 0xFF66BB6A), // 2x1
        BlockShape(listOf(Position(0,0), Position(0,1), Position(0,2)), 0xFFFFA726), // 1x3
        BlockShape(listOf(Position(0,0), Position(1,0), Position(2,0)), 0xFFFFA726), // 3x1
        BlockShape(listOf(Position(0,0), Position(0,1), Position(0,2), Position(0,3)), 0xFF29B6F6), // 1x4
        BlockShape(listOf(Position(0,0), Position(1,0), Position(2,0), Position(3,0)), 0xFF29B6F6), // 4x1
        BlockShape(listOf(Position(0,0), Position(0,1), Position(1,0), Position(1,1)), 0xFFAB47BC), // 2x2 Square
        BlockShape(listOf(Position(0,0), Position(1,0), Position(1,1)), 0xFFEF5350), // L small
        BlockShape(listOf(Position(0,0), Position(1,0), Position(2,0), Position(2,1)), 0xFF26A69A), // L long
        BlockShape(listOf(Position(0,0), Position(0,1), Position(0,2), Position(1,2), Position(2,2)), 0xFFEC407A), // Corner
    )

    init {
        generateNewBlocks()
    }

    private fun generateNewBlocks() {
        availableBlocks = List(3) { shapes[Random.nextInt(shapes.size)] }
    }

    fun selectBlock(index: Int) {
        if (index in availableBlocks.indices) {
            selectedBlockIndex = index
            isPlacingBlock = true
            cursorPosition = Position(gridSize / 2, gridSize / 2)
        }
    }

    fun cancelSelection() {
        isPlacingBlock = false
        selectedBlockIndex = -1
    }

    fun moveCursor(dRow: Int, dCol: Int) {
        val newRow = (cursorPosition.row + dRow).coerceIn(0, gridSize - 1)
        val newCol = (cursorPosition.col + dCol).coerceIn(0, gridSize - 1)
        cursorPosition = Position(newRow, newCol)
    }

    fun tryPlaceBlock(): Boolean {
        if (selectedBlockIndex == -1) return false
        val shape = availableBlocks[selectedBlockIndex]
        
        if (canPlace(shape, cursorPosition)) {
            placeBlock(shape, cursorPosition)
            
            val newAvailable = availableBlocks.toMutableList()
            newAvailable.removeAt(selectedBlockIndex)
            availableBlocks = newAvailable
            
            selectedBlockIndex = -1
            isPlacingBlock = false
            
            checkAndClearLines()
            
            if (availableBlocks.isEmpty()) {
                generateNewBlocks()
            }
            return true
        }
        return false
    }

    fun canPlace(shape: BlockShape, pos: Position): Boolean {
        for (p in shape.points) {
            val r = pos.row + p.row
            val c = pos.col + p.col
            if (r !in 0 until gridSize || c !in 0 until gridSize || grid[r][c] != 0L) {
                return false
            }
        }
        return true
    }

    private fun placeBlock(shape: BlockShape, pos: Position) {
        val newGrid = grid.map { it.copyOf() }.toTypedArray()
        for (p in shape.points) {
            newGrid[pos.row + p.row][pos.col + p.col] = shape.color
        }
        grid = newGrid
        score += shape.points.size * 10
    }

    private fun checkAndClearLines() {
        val rowsToClear = mutableListOf<Int>()
        val colsToClear = mutableListOf<Int>()

        // Check rows
        for (r in 0 until gridSize) {
            if (grid[r].all { it != 0L }) rowsToClear.add(r)
        }

        // Check columns
        for (c in 0 until gridSize) {
            var full = true
            for (r in 0 until gridSize) {
                if (grid[r][c] == 0L) {
                    full = false
                    break
                }
            }
            if (full) colsToClear.add(c)
        }

        if (rowsToClear.isNotEmpty() || colsToClear.isNotEmpty()) {
            val newGrid = grid.map { it.copyOf() }.toTypedArray()
            
            for (r in rowsToClear) {
                for (c in 0 until gridSize) newGrid[r][c] = 0L
            }
            
            for (c in colsToClear) {
                for (r in 0 until gridSize) newGrid[r][c] = 0L
            }
            
            grid = newGrid
            
            // Scoring logic:
            // 1 line = 100 pts
            // 2 lines = 300 pts (Bonus)
            // 3 lines = 600 pts (Combo!)
            val totalCleared = rowsToClear.size + colsToClear.size
            score += when (totalCleared) {
                1 -> 100
                2 -> 300
                3 -> 600
                else -> totalCleared * 250
            }
        }
    }
}
