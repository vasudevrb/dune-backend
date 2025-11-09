package com.vasurb.model

interface AgentCard: Card {
    override val fileName: String
    override val type: Card.Type
}
