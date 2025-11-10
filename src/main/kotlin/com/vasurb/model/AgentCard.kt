package com.vasurb.model

interface AgentCard: Card {
    override val url: String
    override val type: Card.Type
}
