package com.vasurb.model

import com.vasurb.model.Card.Source
import com.vasurb.model.Card.Source.UPRISING
import com.vasurb.model.Card.Type.INTRIGUE
import com.vasurb.util.CardsUrlRetriever

data class IntrigueCard(
    override val url: String,
    override val source: Source
) : Card {
    override val type: Card.Type = INTRIGUE

    class All {
        fun get(allowedSources: List<Source> = listOf(UPRISING)): ArrayList<IntrigueCard> {
            val intrigues = Source.entries
                .filter { allowedSources.contains(it) }
                .flatMap { source ->
                    CardsUrlRetriever.getImageUrls(INTRIGUE, source)
                        .map { url -> IntrigueCard(url, source) }
                }
                .toMutableList() as ArrayList<IntrigueCard>

            return ArrayList(intrigues)
        }
    }
}
