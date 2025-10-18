package com.vasurb.model

data class Game(
    val board: Board = Board.INITIAL_STATE,
    val players: ArrayList<Player> = arrayListOf()
)
