@file:OptIn(ExperimentalTvMaterial3Api::class)

package com.hanyajasa.blockblast

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*
import com.hanyajasa.blockblast.ui.theme.BlockBlastTheme

class MainActivity : ComponentActivity() {
    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BlockBlastTheme {
                // Using Modifier.background to set the container color 
                // as SurfaceDefaults.colors is causing an unresolved reference in this version.
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF121212))
                        .onKeyEvent { handleKeyEvent(it) },
                    shape = RectangleShape
                ) {
                    GameScreen(viewModel)
                }
            }
        }
    }

    private fun handleKeyEvent(event: KeyEvent): Boolean {
        if (event.type != KeyEventType.KeyDown) return false

        return when (event.key) {
            Key.DirectionUp -> {
                if (viewModel.isPlacingBlock) {
                    viewModel.moveCursor(-1, 0)
                    true
                } else false
            }
            Key.DirectionDown -> {
                if (viewModel.isPlacingBlock) {
                    viewModel.moveCursor(1, 0)
                    true
                } else false
            }
            Key.DirectionLeft -> {
                if (viewModel.isPlacingBlock) {
                    viewModel.moveCursor(0, -1)
                } else {
                    val next = (viewModel.selectedBlockIndex - 1).coerceAtLeast(0)
                    viewModel.selectedBlockIndex = next
                }
                true
            }
            Key.DirectionRight -> {
                if (viewModel.isPlacingBlock) {
                    viewModel.moveCursor(0, 1)
                } else {
                    val next = (viewModel.selectedBlockIndex + 1).coerceAtMost(viewModel.availableBlocks.size - 1)
                    if (viewModel.availableBlocks.isNotEmpty()) {
                        viewModel.selectedBlockIndex = next
                    }
                }
                true
            }
            Key.DirectionCenter, Key.Enter -> {
                if (viewModel.isPlacingBlock) {
                    viewModel.tryPlaceBlock()
                } else if (viewModel.selectedBlockIndex != -1 && viewModel.selectedBlockIndex < viewModel.availableBlocks.size) {
                    viewModel.selectBlock(viewModel.selectedBlockIndex)
                } else if (viewModel.selectedBlockIndex == -1 && viewModel.availableBlocks.isNotEmpty()) {
                    viewModel.selectedBlockIndex = 0
                }
                true
            }
            Key.Back, Key.Escape -> {
                if (viewModel.isPlacingBlock) {
                    viewModel.cancelSelection()
                    true
                } else false
            }
            else -> false
        }
    }
}

@Composable
fun GameScreen(viewModel: GameViewModel) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp
    
    // Adaptive cell size based on screen width
    val screenWidth = configuration.screenWidthDp.dp
    val cellSize = if (isLandscape) 40.dp else (screenWidth - 48.dp) / 8

    if (isLandscape) {
        // TV / Landscape Layout
        Row(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Block Blast", style = MaterialTheme.typography.displaySmall)
                Text(text = "Score: ${viewModel.score}", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(24.dp))
                BlockPool(viewModel)
                ControlHints(viewModel)
            }
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                GameGrid(viewModel, cellSize)
            }
        }
    } else {
        // Mobile / Portrait Layout
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Block Blast", style = MaterialTheme.typography.headlineMedium)
                Text(text = "Score: ${viewModel.score}", style = MaterialTheme.typography.headlineSmall)
            }
            
            GameGrid(viewModel, cellSize)
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                BlockPool(viewModel)
                ControlHints(viewModel)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ControlHints(viewModel: GameViewModel) {
    Spacer(modifier = Modifier.height(16.dp))
    if (viewModel.isPlacingBlock) {
        Text(text = "Tap Grid to Place", style = MaterialTheme.typography.bodyMedium, color = Color.Cyan)
        Button(onClick = { viewModel.cancelSelection() }, modifier = Modifier.padding(top = 8.dp)) {
            Text("Cancel")
        }
    } else {
        Text(text = "Pick a block below", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
    }
}

@Composable
fun GameGrid(viewModel: GameViewModel, cellSize: Dp) {
    Column(
        modifier = Modifier
            .background(Color(0xFF1A1A1A), RoundedCornerShape(8.dp))
            .padding(4.dp)
            .border(2.dp, Color.DarkGray, RoundedCornerShape(8.dp))
    ) {
        for (r in 0 until viewModel.gridSize) {
            Row {
                for (c in 0 until viewModel.gridSize) {
                    val cellColor = viewModel.grid[r][c]
                    
                    var ghostColor: Color? = null
                    if (viewModel.isPlacingBlock && viewModel.selectedBlockIndex != -1) {
                        val shape = viewModel.availableBlocks[viewModel.selectedBlockIndex]
                        val isPartOfGhost = shape.points.any { 
                            viewModel.cursorPosition.row + it.row == r && 
                            viewModel.cursorPosition.col + it.col == c 
                        }
                        if (isPartOfGhost) {
                            ghostColor = Color(shape.color).copy(alpha = 0.5f)
                        }
                    }

                    val isCursor = viewModel.isPlacingBlock && viewModel.cursorPosition.row == r && viewModel.cursorPosition.col == c
                    
                    Box(
                        modifier = Modifier
                            .size(cellSize)
                            .padding(1.5.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                when {
                                    cellColor != 0L -> Color(cellColor)
                                    ghostColor != null -> ghostColor
                                    else -> Color(0xFF333333)
                                }
                            )
                            .border(
                                width = if (isCursor) 2.dp else 0.dp,
                                color = if (isCursor) Color.White else Color.Transparent,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .clickable {
                                if (viewModel.isPlacingBlock) {
                                    viewModel.cursorPosition = Position(r, c)
                                    viewModel.tryPlaceBlock()
                                }
                            }
                    )
                }
            }
        }
    }
}

@Composable
fun BlockPool(viewModel: GameViewModel) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        viewModel.availableBlocks.forEachIndexed { index, shape ->
            val isSelected = !viewModel.isPlacingBlock && viewModel.selectedBlockIndex == index
            val isBeingPlaced = viewModel.isPlacingBlock && viewModel.selectedBlockIndex == index
            
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .background(
                        if (isSelected) Color.White.copy(alpha = 0.1f) else Color.Transparent,
                        RoundedCornerShape(12.dp)
                    )
                    .border(
                        width = if (isSelected) 3.dp else 1.dp,
                        color = if (isSelected) Color(0xFF00E5FF) else if (isBeingPlaced) Color.Yellow else Color(0xFF444444),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable {
                        if (!viewModel.isPlacingBlock) {
                            viewModel.selectBlock(index)
                        } else if (viewModel.selectedBlockIndex == index) {
                            viewModel.cancelSelection()
                        }
                    }
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                MiniBlockPreview(shape)
            }
        }
    }
}

@Composable
fun MiniBlockPreview(shape: BlockShape) {
    val cellSize = 12.dp
    if (shape.points.isEmpty()) return
    
    val minR = shape.points.minOf { it.row }
    val maxR = shape.points.maxOf { it.row }
    val minC = shape.points.minOf { it.col }
    val maxC = shape.points.maxOf { it.col }
    
    val width = (maxC - minC + 1) * 12
    val height = (maxR - minR + 1) * 12

    Box(modifier = Modifier.size(width.dp, height.dp)) {
        shape.points.forEach { p ->
            Box(
                modifier = Modifier
                    .offset(x = ((p.col - minC) * 12).dp, y = ((p.row - minR) * 12).dp)
                    .size(cellSize)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(shape.color))
                    .border(0.5.dp, Color.Black.copy(alpha = 0.3f), RoundedCornerShape(3.dp))
            )
        }
    }
}
