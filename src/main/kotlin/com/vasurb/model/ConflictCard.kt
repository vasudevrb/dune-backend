package com.vasurb.model

import com.vasurb.util.CardsUrlRetriever
import com.vasurb.util.CardsUrlRetriever.Key

data class ConflictCard(
    override val url: String,
    val conflictType: ConflictType,
): Card {
    override val type: Card.Type = Card.Type.CONFLICT

    enum class ConflictType(val level: Int) {
        LEVEL_1(1), LEVEL_2(2), LEVEL_3(3)
    }

    companion object{
        fun getConflicts(): ArrayList<ConflictCard>{
            val level1 = CardsUrlRetriever
                .cardImageUrls[Key(Card.Type.CONFLICT, ConflictType.LEVEL_1.name)]
                ?.map { url -> ConflictCard(url, ConflictType.LEVEL_1) }
                ?.shuffled()
                ?.toMutableList() as ArrayList<ConflictCard>

            val level2 = CardsUrlRetriever
                .cardImageUrls[Key(Card.Type.CONFLICT, ConflictType.LEVEL_2.name)]
                ?.map { url -> ConflictCard(url, ConflictType.LEVEL_2) }
                ?.shuffled()
                ?.toMutableList() as ArrayList<ConflictCard>

            val level3 = CardsUrlRetriever
                .cardImageUrls[Key(Card.Type.CONFLICT, ConflictType.LEVEL_3.name)]
                ?.map { url -> ConflictCard(url, ConflictType.LEVEL_3) }
                ?.shuffled()
                ?.toMutableList() as ArrayList<ConflictCard>

            return level1.take(1)
                .plus(level2.take(4))
                .plus(level3)
                .toMutableList() as ArrayList<ConflictCard>
        }
    }
}
