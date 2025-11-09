package com.vasurb.model

interface Card {
    val fileName: String
    val type: Card.Type

    enum class Type {
        IMPERIUM, INTRIGUE, CONFLICT, STARTER
    }

    companion object Card {
        fun getAllCards(type: Card.Type): List<Card> {
            return emptyList()
        }
    }
}
