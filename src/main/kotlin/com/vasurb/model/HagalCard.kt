package com.vasurb.model

import com.vasurb.util.CardsUrlRetriever
import com.vasurb.util.CardsUrlRetriever.Key

data class HagalCard(
    override val url: String
): AgentCard  {

    override val type: Card.Type = Card.Type.HAGAL

    class All {
        fun get(): ArrayList<HagalCard> {
            val cards = CardsUrlRetriever.cardImageUrls[Key(Card.Type.HAGAL)]
                ?.map { url -> HagalCard(url) }
                ?.toMutableList() as ArrayList<HagalCard>

            return ArrayList(cards)
        }
    }
}
