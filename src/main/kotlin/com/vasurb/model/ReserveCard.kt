package com.vasurb.model

import com.vasurb.model.Card.Source.UPRISING
import com.vasurb.util.CardsUrlRetriever
import com.vasurb.util.CardsUrlRetriever.ReserveKey

data class ReserveCard(
    override val url: String,
    val reserveType: ReserveType,
    override val source: Card.Source = UPRISING
) : AgentCard {
    override val type: Card.Type = Card.Type.RESERVE

    enum class ReserveType {
        THE_SPICE_MUST_FLOW, PREPARE_THE_WAY
    }

    class All {
        fun get(): Map<ReserveType, ArrayList<ReserveCard>> {
            return ReserveType.entries.associateWith { type ->
                CardsUrlRetriever.getReserveImageUrls(type)
                    .map { url -> ReserveCard(url, type) }
                    .toMutableList() as ArrayList<ReserveCard>
            }
        }
    }
}

