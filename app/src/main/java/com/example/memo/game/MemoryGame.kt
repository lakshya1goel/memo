package com.example.memo.game

import com.example.memo.model.MemoryCard

class MemoryGame {
    var cards: List<MemoryCard> = emptyList()
        private set

    var score: Int = 0
        private set

    init {
        resetGame()
    }

    fun resetGame() {
        cards = List(16) {
            index ->
            MemoryCard(
                id = index,
                iconId = index / 2
            )
        }.shuffled()

        score = 0
    }
}