package com.vasurb.websocket

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.vasurb.api.model.Action
import com.vasurb.api.model.Action.Type.*
import com.vasurb.api.model.ActionResponse
import com.vasurb.service.GameService
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler

@Component
class GameWebSocketHandler(val gameService: GameService): TextWebSocketHandler() {

    val mapper = jacksonObjectMapper()


    override fun afterConnectionEstablished(session: WebSocketSession) {
        println("Connection established with id ${session.id}")
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        println("Connection closed with id ${session.id}")

        val player = gameService.getGame(getGameId(session)).players
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
            START_GAME -> handleStartGameMessage(session, action)

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

    private fun handleStartGameMessage(session: WebSocketSession, action: Action) {
        val players = gameService.getGame(getGameId(session)).players
        val response = ActionResponse(START_GAME, null)
        players.map { it.session }
            .forEach { it?.sendMessage(TextMessage(mapper.writeValueAsString(response))) }
    }

    private fun getGameId(session: WebSocketSession): String {
        val path = session.uri?.path
        return path?.substring(path.lastIndexOf('/') + 1) ?: "ERR"
    }
}
