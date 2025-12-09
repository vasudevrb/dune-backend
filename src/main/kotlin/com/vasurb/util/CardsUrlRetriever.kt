package com.vasurb.util

import com.vasurb.model.Card
import com.vasurb.model.ConflictCard
import com.vasurb.model.ReserveCard
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.stereotype.Component

@Component
class CardsUrlRetriever(@Value("\${server_url}") val serverUrl: String) : CommandLineRunner {

    data class Key(
        val first: Card.Type,
        val second: String? = null,
    )

    override fun run(vararg args: String?) {
        val resolver = PathMatchingResourcePatternResolver()

        Card.Type.entries
            .map { cardType ->
                if (cardType == Card.Type.CONFLICT) {
                    ConflictCard.ConflictType.entries
                        .map { conflictLevel ->
                            cardImageUrls[Key(cardType, conflictLevel.name)] = getUrls(getConflictDirectory(conflictLevel), resolver)
                        }

                } else if (cardType == Card.Type.RESERVE) {
                    ReserveCard.ReserveType.entries
                        .map {reserveType ->
                            cardImageUrls[Key(cardType, reserveType.name)] = getUrls(getReserveDirectory(reserveType), resolver)
                        }
                } else {
                    cardImageUrls[Key(cardType)] = getUrls(getDirectoryByCardType(cardType), resolver)
                }
            }

        print(cardImageUrls)
    }

    fun getUrls(directory: String, resolver: PathMatchingResourcePatternResolver): List<String> {
        val classPathDir = "classpath:/static/${directory}/*"
        return resolver.getResources(classPathDir)
            .filter { it.exists() }
            .map { "${serverUrl}/${directory}/${it.filename}" }
    }

    fun getDirectoryByCardType(cardType: Card.Type): String {
        return when (cardType) {
            Card.Type.IMPERIUM -> "imperium_cards"
            Card.Type.INTRIGUE -> "intrigue_cards"
            Card.Type.STARTER -> "imperium_cards_starter"
            Card.Type.CONTRACT -> "contracts"
            else -> "no_op"
        }
    }

    fun getConflictDirectory(conflictType: ConflictCard.ConflictType): String {
        return when (conflictType) {
            ConflictCard.ConflictType.LEVEL_1 -> "conflict_cards/level_1"
            ConflictCard.ConflictType.LEVEL_2 -> "conflict_cards/level_2"
            ConflictCard.ConflictType.LEVEL_3 -> "conflict_cards/level_3"
        }
    }

    fun getReserveDirectory(reserveType: ReserveCard.ReserveType): String {
        return when (reserveType) {
            ReserveCard.ReserveType.THE_SPICE_MUST_FLOW -> "reserve_cards/the_spice_must_flow"
            ReserveCard.ReserveType.PREPARE_THE_WAY -> "reserve_cards/prepare_the_way"
        }
    }

    companion object {
        val cardImageUrls = mutableMapOf<Key, List<String>>()
    }
}
