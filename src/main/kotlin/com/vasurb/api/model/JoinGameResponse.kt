package com.vasurb.api.model

data class JoinGameResponse(
    val gameId: String?,
    val joinGameState: JoinGameState
) {
    enum class JoinGameState {
        JOINED, PREVIOUSLY_JOINED, IN_LOBBY, IN_GAME, CANNOT_JOIN_MAX_PLAYERS, CANNOT_JOIN_GAME_STARTED
    }
}
