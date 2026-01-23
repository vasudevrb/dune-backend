package com.vasurb.model

import com.vasurb.model.Card.Source.UPRISING
import com.vasurb.model.Card.Type.HAGAL
import com.vasurb.util.CardsUrlRetriever
import com.vasurb.util.CardsUrlRetriever.Key

data class HagalCard(
    override val url: String,
    override val source: Card.Source = UPRISING,
): AgentCard  {

    override val type: Card.Type = HAGAL

    class All {
        fun get(): ArrayList<HagalCard> {
            val cards = CardsUrlRetriever.getImageUrls(HAGAL, UPRISING)
                .map { url -> HagalCard(url) }
                .toMutableList() as ArrayList<HagalCard>

            return ArrayList(cards)
        }
    }
}
