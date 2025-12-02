package com.vasurb.model

import com.vasurb.util.CardsUrlRetriever

data class ContractCard(
    override val url: String,
): Card {
    override val type: Card.Type = Card.Type.CONTRACT

    class All {
        fun get(): ArrayList<ContractCard> {
            val cards = CardsUrlRetriever.cardImageUrls[CardsUrlRetriever.Key(Card.Type.CONTRACT)]
                ?.map { url -> ContractCard(url) }
                ?.toMutableList() as ArrayList<ContractCard>

            return ArrayList(cards)
        }
    }
}
