package com.vasurb.api.model

import com.fasterxml.jackson.annotation.JsonValue
import com.vasurb.model.PlayableCharacter
import com.vasurb.model.PlayableCharacter.*


data class CharactersResponse(val characters: List<Character>) {
    @JsonValue
    fun value() = characters
}

data class Character(
    val characterName: PlayableCharacter,
    val urls: List<String>,
    val avatarUrl: String
)
