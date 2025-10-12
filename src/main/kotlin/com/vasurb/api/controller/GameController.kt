package com.vasurb.api.controller

import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.simp.user.SimpUserRegistry
import org.springframework.web.bind.annotation.RestController


@RestController
class GameController(private val simpUserRegistry: SimpUserRegistry) {

    @MessageMapping("/action")
    @SendTo("/game-loop")
    fun action(@Payload message: String): String {
        return ""
    }
}
