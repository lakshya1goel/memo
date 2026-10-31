package com.example.memo.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.memo.model.MemoryCard

class MemoryGame {
    var cards by mutableStateOf<List<MemoryCard>>(emptyList())
        private set

    var score by mutableIntStateOf(0)
        private set

    init {
        resetGame()
    }

    fun resetGame() {
        cards = List(16) { index ->
            MemoryCard(
                id = index,
                iconId = index / 2
            )
        }.shuffled()

        score = 0
    }

    fun selectCard(cardId: Int) {
        val selectedCard = cards.find { it.id == cardId } ?: return

        // Ignore a card that is already visible or matched.
        if (selectedCard.isFacedUp || selectedCard.isMatched) {
            return
        }

        // Allow only two unmatched cards to be visible.
        val openCards = cards.count {
            it.isFacedUp && !it.isMatched
        }

        if (openCards >= 2) {
            return
        }

        cards = cards.map { card ->
            if (card.id == cardId) {
                card.copy(isFacedUp = true)
            } else {
                card
            }
        }
    }
}