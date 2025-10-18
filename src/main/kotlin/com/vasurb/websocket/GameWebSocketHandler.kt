package com.vasurb.websocket

import com.vasurb.model.Player
import com.vasurb.service.GameService
import org.springframework.stereotype.Component
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler

@Component
class GameWebSocketHandler(val gameService: GameService): TextWebSocketHandler() {

    override fun afterConnectionEstablished(session: WebSocketSession) {
        println("Connection established with id ${session.id}")
        gameService.getGame(getGameId(session))
            .players
            .add(Player(session.id))
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        println("Message received from ${session.id}: ${message.payload}")
        session.sendMessage(TextMessage("Hello!"))
    }

    private fun getGameId(session: WebSocketSession): String {
        val path = session.uri?.path
        return path?.substring(path.lastIndexOf('/') + 1) ?: "ERR"
    }
}
