package com.vasurb.api.auth

import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.MessageBuilder
import org.springframework.messaging.support.MessageHeaderAccessor
import org.springframework.stereotype.Component


@Component
class PlayerHandshakeInterceptor: ChannelInterceptor {

    override fun preSend(
        message: Message<*>,
        channel: MessageChannel
    ): Message<*>? {
        val accessor = MessageHeaderAccessor
            .getAccessor(message, StompHeaderAccessor::class.java)!!
        if (StompCommand.CONNECT == accessor.command) {
            val playerId = accessor.getFirstNativeHeader("player-name") ?: "guest"
            accessor.user = PlayerPrincipal(playerId)
        }
        return MessageBuilder.createMessage(message.payload, accessor.messageHeaders)
    }
}
