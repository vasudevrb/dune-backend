package com.vasurb.api.model

data class JoinGameResponse(
    val gameId: String,
    val alreadyPresentInGame: Boolean = false,
)
