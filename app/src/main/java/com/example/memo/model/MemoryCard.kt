package com.example.memo.model

data class MemoryCard (
    val id: Int,
    val iconId: Int,
    val isFacedUp: Boolean = false,
    val isMatched: Boolean = false
)