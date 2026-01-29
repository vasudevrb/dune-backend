package com.vasurb.model

import com.vasurb.model.Card.Source.BLOODLINES
import com.vasurb.model.Card.Source.UPRISING
import com.vasurb.model.Card.Type.STARTER
import com.vasurb.util.CardsUrlRetriever

data class StarterCard(
    override val url: String,
    override val source: Card.Source = BLOODLINES
): AgentCard {

    override val type: Card.Type = STARTER

    companion object {
        fun getAll(): ArrayList<AgentCard> {
            val cards = CardsUrlRetriever.getImageUrls(STARTER, UPRISING)
                .map { url -> StarterCard(url) }
                .toMutableList() as ArrayList<StarterCard>

            return ArrayList(cards)
        }

    }
}
