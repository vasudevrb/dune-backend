package com.vasurb.api.model

import com.vasurb.model.Game


data class JoinGameResponse(
    val gameId: String?,
    val joinGameState: JoinGameState,
    val turnOrder: Int,
    val game: Game
) {
    enum class JoinGameState {
        JOINED,
        PREVIOUSLY_JOINED,
        IN_LOBBY,
        IN_GAME,
        CANNOT_JOIN_MAX_PLAYERS,
        CANNOT_JOIN_GAME_STARTED
    }
}
