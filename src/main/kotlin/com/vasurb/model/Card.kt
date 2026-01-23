package com.vasurb.model

interface Card {
    val url: String
    val type: Type

    enum class Type {
        IMPERIUM, INTRIGUE, CONFLICT, STARTER, RESERVE, CONTRACT, HAGAL, TECH, SARDAUKAR_SKILL
    }

    enum class Source {
        HAND, PLAY, DISCARD, INTRIGUE
    }
}
