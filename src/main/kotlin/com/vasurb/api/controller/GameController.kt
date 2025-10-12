package com.vasurb.api.controller

import com.vasurb.api.model.Action
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class GameController {

    @MessageMapping("/action")
    @SendTo("/game-loop")
    fun action(@RequestBody action: Action): String {
        return ""
    }
}
