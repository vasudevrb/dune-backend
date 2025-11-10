package com.vasurb.model

import com.vasurb.util.CardsUrlRetriever
import com.vasurb.util.CardsUrlRetriever.Key

data class IntrigueCard(
    override val url: String
): Card {
    override val type: Card.Type = Card.Type.INTRIGUE

    companion object {
        fun getAll(): ArrayList<IntrigueCard> {
            return CardsUrlRetriever.cardImageUrls[Key(Card.Type.INTRIGUE)]
                ?.map { url -> IntrigueCard(url) }
                ?.toMutableList() as ArrayList<IntrigueCard>
        }
    }
}
