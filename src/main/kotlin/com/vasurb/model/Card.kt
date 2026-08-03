package com.vasurb.model

interface Card {
    val url: String
    val type: Type
    val source: Source

    enum class Type {
        IMPERIUM, INTRIGUE, CONFLICT, STARTER, RESERVE, CONTRACT, HAGAL, TECH, SARDAUKAR_SKILL, NAVIGATION, TWISTED_INTRIGUE, RAID
    }

    enum class SourceDeck {
        HAND, PLAY, DISCARD, INTRIGUE
    }

    enum class Source {
        UPRISING, BLOODLINES, CONSPIRACY
    }
}
