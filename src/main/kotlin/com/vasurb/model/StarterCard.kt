package com.vasurb.model

data class StarterCard(
    override val id: Int,
    override val fileName: String
): Card {

    override val type: Card.Type = Card.Type.STARTER
}
