package com.vasurb.websocket

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.vasurb.api.model.Action
import com.vasurb.api.model.Action.Type.*
import com.vasurb.api.model.ActionResponse
import com.vasurb.model.Game
import com.vasurb.model.Player
import com.vasurb.service.GameService
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler

@Component
class DuneWsHandler(val gameService: GameService) : TextWebSocketHandler() {

    val mapper = jacksonObjectMapper()


    override fun afterConnectionEstablished(session: WebSocketSession) {
        println("Connection established with id ${session.id}")
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        println("Connection closed with id ${session.id}")

        val player = getGame(session).players
            .find { it.session?.id == session.id }

        player?.session = null
        handleGetCharacterReadyStates(session)
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        println("Message received from ${session.id}: ${message.payload}")

        val action: Action = mapper.readValue(message.payload)
        when (action.type) {
            ADD_TO_GAME -> handleAddToGameAction(session, action)
            GET_CHARACTER_READY_STATES -> handleGetCharacterReadyStates(session)
            START_GAME -> handleStartGameMessage(session)
            else -> println("Unknown action type: ${action.type}")
        }
    }

    private fun handleGetCharacterReadyStates(session: WebSocketSession) {
        val players = gameService.getGame(getGameId(session)).players

        val responseBody = mapper.createArrayNode().apply {
            players.forEach {
                add(mapper.createObjectNode().apply {
                    put("name", it.name)
                    put("color", it.color.name)
                    put("characterName", it.character.readableName)
                    put("status", if (it.session != null) "Ready" else "Not ready")
                })
            }
        }

        val response = ActionResponse(
            GET_CHARACTER_READY_STATES,
            responseBody
        )

        players.map { it.session }
            .forEach { it?.sendMessage(TextMessage(mapper.writeValueAsString(response))) }
    }

    private fun handleAddToGameAction(session: WebSocketSession, action: Action) {
        val playerName = action.body?.get("playerName")?.asText()
            ?: throw RuntimeException("Incorrect action body: $action")

        val player = gameService.getPlayer(getGameId(session), playerName)
        player.session = session
    }

    private fun handleStartGameMessage(session: WebSocketSession) {
        broadcastToEachPlayer(
            game = getGame(session),
            message = ActionResponse(START_GAME, null)
        )
    }

    private fun broadcastToEachPlayer(
        gameId: String? = null,
        players: List<Player>? = null,
        game: Game? = null,
        message: Any
    ) {
        val allPlayers = when {
            players != null -> players
            game != null -> game.players
            gameId != null -> gameService.getGame(gameId).players
            else -> throw IllegalArgumentException("Must provide at least one of (gameId, players, game)")
        }

        allPlayers.map { it.session }
            .forEach { it?.sendMessage(TextMessage(mapper.writeValueAsString(message))) }
    }

    private fun getGameId(session: WebSocketSession): String {
        val path = session.uri?.path
        return path?.substring(path.lastIndexOf('/') + 1) ?: "ERR"
    }

    private fun getGame(session: WebSocketSession) = gameService.getGame(getGameId(session))
}
