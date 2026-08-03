package com.vasurb.model

import com.vasurb.model.Card.Source
import com.vasurb.model.Card.Source.CONSPIRACY
import com.vasurb.util.CardsUrlRetriever

data class RaidToken(
    override val url: String,
    override val source: Source,
): Card {
    override val type: Card.Type = Card.Type.RAID

    enum class RaidType {
        LARGE, SMALL
    }

    class All {
        fun get(allowedSources: List<Source> = listOf(CONSPIRACY), raidType: RaidType): ArrayList<RaidToken> {
            if (allowedSources.contains(CONSPIRACY)) {
                return CardsUrlRetriever.getRaidTokenImageUrls(CONSPIRACY, raidType)
                    .map { url -> RaidToken(url, CONSPIRACY) }
                    .toMutableList() as ArrayList<RaidToken>
            }

            return arrayListOf()
        }
    }
}
