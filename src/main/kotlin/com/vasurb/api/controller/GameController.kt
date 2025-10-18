package com.vasurb.api.controller

import com.vasurb.api.model.Character
import com.vasurb.api.model.CharactersRequestBody
import com.vasurb.api.model.CharactersResponse
import com.vasurb.service.GameService
import com.vasurb.util.Util
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class GameController(val gameService: GameService) {

    @PostMapping("/get-characters")
    fun getCharacters(@RequestBody body: CharactersRequestBody): CharactersResponse {
        val characters = gameService.presentedCharacters[body.playerName] ?: gameService.availableCharacters
            .shuffled(Util.getDeterministicRandom(body.playerName))
            .take(NUM_PICKABLE_CHARACTERS)

        //TODO: Handle when characters is an empty list?
        characters.forEach { gameService.availableCharacters.remove(it) }
        gameService.presentedCharacters[body.playerName] = characters
        return CharactersResponse(characters.map { Character(it.name) })
    }

    companion object {
        const val NUM_PICKABLE_CHARACTERS = 2
    }
}
