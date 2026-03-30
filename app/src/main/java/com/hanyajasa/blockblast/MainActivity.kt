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
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.*
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
    
    val screenWidth = configuration.screenWidthDp.dp
    val cellSize = if (isLandscape) 36.dp else (screenWidth - 48.dp) / 8

    if (isLandscape) {
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
                
                if (viewModel.isMultiplayer) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Opponent: ${viewModel.opponentScore}", color = Color.Red, style = MaterialTheme.typography.headlineSmall)
                }

                Spacer(modifier = Modifier.height(16.dp))
                BlockPool(viewModel)
                ControlHints(viewModel)
                MultiplayerControls(viewModel)
            }

            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                GameGrid(viewModel.grid, viewModel, cellSize)
            }

            if (viewModel.isMultiplayer) {
                Box(modifier = Modifier.weight(0.7f), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Opponent's Grid", style = MaterialTheme.typography.bodySmall)
                        GameGrid(viewModel.opponentGrid, viewModel, cellSize * 0.6f, isOpponent = true)
                    }
                }
            }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = "Block Blast", style = MaterialTheme.typography.headlineMedium)
                    Text(text = "Score: ${viewModel.score}", style = MaterialTheme.typography.headlineSmall)
                }
                if (viewModel.isMultiplayer) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "Opponent", style = MaterialTheme.typography.bodySmall)
                        Text(text = "${viewModel.opponentScore}", color = Color.Red, style = MaterialTheme.typography.headlineSmall)
                    }
                }
            }
            
            GameGrid(viewModel.grid, viewModel, cellSize)
            
            if (viewModel.isMultiplayer) {
                Text("Opponent's Grid", style = MaterialTheme.typography.labelSmall)
                GameGrid(viewModel.opponentGrid, viewModel, cellSize * 0.4f, isOpponent = true)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                BlockPool(viewModel)
                ControlHints(viewModel)
                MultiplayerControls(viewModel)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun MultiplayerControls(viewModel: GameViewModel) {
    var showJoinDialog by remember { mutableStateOf(false) }
    var ipInput by remember { mutableStateOf("192.168.1. ") }

    if (!viewModel.isMultiplayer) {
        Row(modifier = Modifier.padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { viewModel.startHost() }) {
                Text("Host Game")
            }
            Button(onClick = { showJoinDialog = true }) {
                Text("Join Game")
            }
        }
    }

    if (showJoinDialog) {
        AlertDialog(
            onDismissRequest = { showJoinDialog = false },
            title = { Text("Join Local Game") },
            text = {
                Column {
                    Text("Enter Host IP Address:")
                    // Note: In a real app, use a proper TextField. 
                    // For now, simplified for the example.
                    Text(ipInput, modifier = Modifier.background(Color.DarkGray).padding(8.dp))
                }
            },
            confirmButton = {
                Button(onClick = { 
                    viewModel.joinGame(ipInput.trim())
                    showJoinDialog = false 
                }) { Text("Connect") }
            }
        )
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
    } else if (!viewModel.isMultiplayer) {
        Text(text = "Pick a block below", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
    }
}

@Composable
fun GameGrid(grid: Array<LongArray>, viewModel: GameViewModel, cellSize: Dp, isOpponent: Boolean = false) {
    Column(
        modifier = Modifier
            .background(Color(0xFF1A1A1A), RoundedCornerShape(8.dp))
            .padding(4.dp)
            .border(2.dp, if (isOpponent) Color.Red else Color.DarkGray, RoundedCornerShape(8.dp))
    ) {
        for (r in 0 until viewModel.gridSize) {
            Row {
                for (c in 0 until viewModel.gridSize) {
                    val cellColor = grid[r][c]
                    
                    var ghostColor: Color? = null
                    if (!isOpponent && viewModel.isPlacingBlock && viewModel.selectedBlockIndex != -1) {
                        val shape = viewModel.availableBlocks[viewModel.selectedBlockIndex]
                        val isPartOfGhost = shape.points.any { 
                            viewModel.cursorPosition.row + it.row == r && 
                            viewModel.cursorPosition.col + it.col == c 
                        }
                        if (isPartOfGhost) {
                            ghostColor = Color(shape.color).copy(alpha = 0.5f)
                        }
                    }

                    val isCursor = !isOpponent && viewModel.isPlacingBlock && viewModel.cursorPosition.row == r && viewModel.cursorPosition.col == c
                    
                    Box(
                        modifier = Modifier
                            .size(cellSize)
                            .padding(if (cellSize > 20.dp) 1.5.dp else 0.5.dp)
                            .clip(RoundedCornerShape(if (cellSize > 20.dp) 4.dp else 1.dp))
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
                            .then(
                                if (!isOpponent) Modifier.clickable {
                                    if (viewModel.isPlacingBlock) {
                                        viewModel.cursorPosition = Position(r, c)
                                        viewModel.tryPlaceBlock()
                                    }
                                } else Modifier
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun BlockPool(viewModel: GameViewModel) {
    Row(
        modifier = Modifier.padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        viewModel.availableBlocks.forEachIndexed { index, block ->
            val isSelected = viewModel.selectedBlockIndex == index
            Box(
                modifier = Modifier
                    .border(
                        width = if (isSelected) 3.dp else 1.dp,
                        color = if (isSelected) Color.White else Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp)
                    .clickable { 
                        viewModel.selectBlock(index)
                    }
            ) {
                SmallBlockPreview(block)
            }
        }
    }
}

@Composable
fun SmallBlockPreview(block: BlockShape) {
    val previewCellSize = 12.dp
    Column {
        for (r in 0 until 5) {
            Row {
                for (c in 0 until 5) {
                    val isPart = block.points.any { it.row == r && it.col == c }
                    Box(
                        modifier = Modifier
                            .size(previewCellSize)
                            .padding(1.dp)
                            .background(if (isPart) Color(block.color) else Color.Transparent, RoundedCornerShape(2.dp))
                    )
                }
            }
        }
    }
}
