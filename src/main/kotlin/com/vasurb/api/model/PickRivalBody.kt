package com.vasurb.api.model

import com.vasurb.model.Color
import com.vasurb.model.PlayableCharacter
import com.vasurb.model.RivalCharacter

data class PickRivalBody(
    val gameId: String,
    val rivalName: RivalCharacter
)
