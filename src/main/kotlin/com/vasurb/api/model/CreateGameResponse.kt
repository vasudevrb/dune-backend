package com.vasurb.api.model

import com.vasurb.model.Game

data class CreateGameResponse(
    val gameId: String,
    val turnOrder: Int,
    val game: Game
)
