package com.vasurb.model

import com.vasurb.model.Card.Source
import com.vasurb.model.Card.Source.UPRISING
import com.vasurb.util.CardsUrlRetriever

data class ContractCard(
    override val url: String,
    override val source: Source,
): Card {
    override val type: Card.Type = Card.Type.CONTRACT

    class All {
        fun get(allowedSources: List<Source> = listOf(UPRISING)): ArrayList<ContractCard> {
            val cards = Source.entries
                .filter { allowedSources.contains(it) }
                .flatMap { source ->
                    CardsUrlRetriever.getImageUrls(Card.Type.CONTRACT, source)
                        .map { url -> ContractCard(url, source) }
                }
                .toMutableList() as ArrayList<ContractCard>

            return ArrayList(cards)
        }
    }
}
