package com.vasurb.model

interface Card {
    val url: String
    val type: Type

    enum class Type {
        IMPERIUM, INTRIGUE, CONFLICT, STARTER, RESERVE, CONTRACT
    }

    enum class Source {
        HAND, PLAY, DISCARD, INTRIGUE
    }
}
