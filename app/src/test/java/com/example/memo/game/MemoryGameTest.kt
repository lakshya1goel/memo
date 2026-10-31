package com.example.memo.game

import org.junit.Assert.*
import org.junit.Test

class MemoryGameTest {
    @Test
    fun matchingPairStaysOpenAndAllowsNextSelection() {
        val game = MemoryGame()
        game.selectCard(0)
        game.selectCard(1)

        assertEquals(2, game.score)
        assertTrue(game.cards.filter { it.id in 0..1 }.all { it.isMatched && it.isFacedUp })
        assertNull(game.pendingMismatchId)

        game.selectCard(2)
        assertTrue(game.cards.single { it.id == 2 }.isFacedUp)
    }

    @Test
    fun mismatchBlocksThirdCardThenClosesAndUnlocks() {
        val game = MemoryGame()
        game.selectCard(0)
        game.selectCard(2)
        val mismatchId = requireNotNull(game.pendingMismatchId)

        game.selectCard(4)
        assertFalse(game.cards.single { it.id == 4 }.isFacedUp)
        assertEquals(2, game.cards.count { it.isFacedUp })
        assertEquals(0, game.score)

        game.hideMismatchedCards(mismatchId)
        assertTrue(game.cards.none { it.isFacedUp })
        assertNull(game.pendingMismatchId)

        game.selectCard(4)
        assertTrue(game.cards.single { it.id == 4 }.isFacedUp)
    }

    @Test
    fun repeatedTapsDoNotMatchACardWithItselfOrScoreTwice() {
        val game = MemoryGame()
        game.selectCard(0)
        game.selectCard(0)
        assertEquals(1, game.cards.count { it.isFacedUp })
        assertEquals(0, game.score)

        game.selectCard(1)
        game.selectCard(0)
        game.selectCard(1)
        assertEquals(2, game.score)
    }

    @Test
    fun closingMismatchPreservesPreviouslyMatchedCards() {
        val game = MemoryGame()
        game.selectCard(0)
        game.selectCard(1)
        game.selectCard(2)
        game.selectCard(4)
        game.hideMismatchedCards(requireNotNull(game.pendingMismatchId))

        assertEquals(2, game.score)
        assertTrue(game.cards.filter { it.id in 0..1 }.all { it.isMatched && it.isFacedUp })
        assertTrue(game.cards.filter { it.id !in 0..1 }.none { it.isFacedUp })
    }

    @Test
    fun oldTimerCannotCloseNewPairAfterRestart() {
        val game = MemoryGame()
        game.selectCard(0)
        game.selectCard(2)
        val oldId = requireNotNull(game.pendingMismatchId)

        game.resetGame()
        assertEquals(0, game.score)
        assertNull(game.pendingMismatchId)
        assertTrue(game.cards.none { it.isFacedUp || it.isMatched })

        game.selectCard(0)
        game.selectCard(2)
        val newId = requireNotNull(game.pendingMismatchId)
        assertNotEquals(oldId, newId)

        game.hideMismatchedCards(oldId)
        assertEquals(newId, game.pendingMismatchId)
        assertEquals(2, game.cards.count { it.isFacedUp })

        game.hideMismatchedCards(newId)
        assertTrue(game.cards.none { it.isFacedUp })
    }

    @Test
    fun allEightPairsCanBeCompletedAndRestarted() {
        val game = MemoryGame()
        for (id in 0 until 16) game.selectCard(id)

        assertEquals(16, game.score)
        assertTrue(game.cards.all { it.isMatched && it.isFacedUp })

        game.resetGame()
        assertEquals(0, game.score)
        assertEquals(16, game.cards.size)
        assertTrue(game.cards.none { it.isMatched || it.isFacedUp })
    }
}
