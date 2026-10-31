package com.example.memo.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.memo.model.MemoryCard

class MemoryGame {
    enum class SelectionResult { IGNORED, REVEALED, MATCH, MISMATCH }

    var cards by mutableStateOf<List<MemoryCard>>(emptyList())
        private set

    var score by mutableIntStateOf(0)
        private set

    // Non-null while a mismatched pair is waiting to close.
    var pendingMismatchId by mutableStateOf<Int?>(null)
        private set

    // Keep increasing across restarts so an old timer cannot close a new pair.
    private var nextMismatchId = 0

    init {
        resetGame()
    }

    fun resetGame() {
        pendingMismatchId = null
        cards = List(16) { index ->
            MemoryCard(
                id = index,
                iconId = index / 2
            )
        }.shuffled()
        score = 0
    }

    fun selectCard(cardId: Int): SelectionResult {
        if (pendingMismatchId != null) return SelectionResult.IGNORED

        val selectedCard = cards.find { it.id == cardId } ?: return SelectionResult.IGNORED
        if (selectedCard.isFacedUp || selectedCard.isMatched) return SelectionResult.IGNORED

        cards = cards.map { card ->
            if (card.id == cardId) card.copy(isFacedUp = true) else card
        }

        val openCards = cards.filter { it.isFacedUp && !it.isMatched }
        if (openCards.size != 2) return SelectionResult.REVEALED

        if (openCards[0].iconId == openCards[1].iconId) {
            val matchedIds = openCards.map { it.id }
            cards = cards.map { card ->
                if (card.id in matchedIds) card.copy(isMatched = true) else card
            }
            score += 2
            return SelectionResult.MATCH
        } else {
            nextMismatchId += 1
            pendingMismatchId = nextMismatchId
            return SelectionResult.MISMATCH
        }
    }

    fun hideMismatchedCards(mismatchId: Int) {
        if (pendingMismatchId != mismatchId) return

        cards = cards.map { card ->
            if (card.isFacedUp && !card.isMatched) {
                card.copy(isFacedUp = false)
            } else {
                card
            }
        }
        pendingMismatchId = null
    }
}
