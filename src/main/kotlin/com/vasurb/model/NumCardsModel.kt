package com.vasurb.model

data class NumCardsModel(
    val inHand: Int = 0,
    val inPlay: Int = 0,
    val inDiscardPile: Int = 0,
    val inDrawPile: Int = 0,
    val intrigues: Int = 0
)
