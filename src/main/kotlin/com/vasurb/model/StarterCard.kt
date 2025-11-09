package com.vasurb.model

import com.vasurb.util.CardsUrlRetriever
import com.vasurb.util.CardsUrlRetriever.Key

data class StarterCard(
    override val fileName: String
): AgentCard {

    override val type: Card.Type = Card.Type.STARTER

    companion object {
        fun getAll(): ArrayList<AgentCard> {
            val cards = CardsUrlRetriever.cardImageUrls[Key(Card.Type.STARTER)]
                ?.map { url -> StarterCard(url) }
                ?.toMutableList() as ArrayList<StarterCard>

            return ArrayList(cards)
        }
    }
}
