package com.vasurb.api.model

import com.vasurb.model.Color
import com.vasurb.model.PlayableCharacter

data class PickCharacterBody(
    val gameId: String,
    val playerName: String,
    val characterName: PlayableCharacter
)
