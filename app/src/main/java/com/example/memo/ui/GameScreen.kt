package com.example.memo.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.example.memo.R
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.memo.game.MemoryGame
import com.example.memo.ui.theme.MemoTheme
import androidx.compose.ui.unit.dp

private val cardImages = listOf(
    R.drawable.card_fox,
    R.drawable.card_panda,
    R.drawable.card_frog,
    R.drawable.card_owl,
    R.drawable.card_cat,
    R.drawable.card_rabbit,
    R.drawable.card_penguin,
    R.drawable.card_lion
)

private val cardNames = listOf(
    "Fox", "Panda", "Frog", "Owl",
    "Cat", "Rabbit", "Penguin", "Lion"
)

@Composable
fun GameScreen(modifier: Modifier = Modifier) {
    val game = remember { MemoryGame() }

    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.game_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )

        Column(
            modifier = modifier.fillMaxSize().padding(16.dp)
        ) {
            Text(
                text = "Memory Game",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFF243D2C)
            )

            Text(
                text = "Score: ${game.score}",
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFF243D2C)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = modifier.fillMaxWidth().weight(1f),
                contentPadding = PaddingValues(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = game.cards,
                    key = { card -> card.id }
                ) { card ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFDFBF5)
                        )
                    ) {
                        Image(
                            painter = painterResource(
                                cardImages[card.iconId]
                            ),
                            contentDescription = cardNames[card.iconId],
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GameScreenPreview() {
    MemoTheme() {
        GameScreen()
    }
}