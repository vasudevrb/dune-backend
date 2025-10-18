package com.vasurb.api.model

data class CharactersResponse(
    val characters: List<Character>
)

data class Character(
    val url: String
)
