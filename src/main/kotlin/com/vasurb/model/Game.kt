package com.vasurb.model

data class Game(
    val board: Board,
    val players: List<Player>,
    val currentPlayer: Player
)
