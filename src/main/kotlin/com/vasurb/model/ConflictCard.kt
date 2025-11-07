package com.vasurb.model

data class ConflictCard(
    override val id: Int,
    override val fileName: String
): Card {
    override val type: Card.Type = Card.Type.CONFLICT
}
