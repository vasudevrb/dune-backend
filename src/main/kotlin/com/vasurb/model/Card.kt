package com.vasurb.model

interface Card {
    val url: String
    val type: Card.Type

    enum class Type {
        IMPERIUM, INTRIGUE, CONFLICT, STARTER
    }
}
