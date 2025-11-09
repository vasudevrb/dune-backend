package com.vasurb.util

import com.vasurb.model.Card
import com.vasurb.model.ConflictCard
import com.vasurb.model.PlayableCharacter
import com.vasurb.model.PlayableCharacter.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.stereotype.Component

@Component
class CardsUrlRetriever(@Value("\${server_url}") val serverUrl: String) : CommandLineRunner {

    data class Key(
        val first: Card.Type,
        val second: ConflictCard.ConflictType? = null,
    )

    override fun run(vararg args: String?) {
        val resolver = PathMatchingResourcePatternResolver();

        Card.Type.entries
            .map { cardType ->
                if (cardType == Card.Type.CONFLICT) {
                    ConflictCard.ConflictType.entries
                        .map { conflictLevel ->
                            val classPathDir = "classpath:/static/${getConflictDirectory(conflictLevel)}/*"
                            cardImageUrls[Key(cardType, conflictLevel)] = getUrls(classPathDir, resolver)
                        }

                } else {
                    val classPathDir = "classpath:/static/${getDirectoryByCardType(cardType)}/*"
                    cardImageUrls[Key(cardType)] = getUrls(classPathDir, resolver)
                }
            }
    }

    fun getUrls(directory: String, resolver: PathMatchingResourcePatternResolver): List<String> {
        return resolver.getResources(directory)
            .filter { it.exists() }
            .map { serverUrl + it.filename }
    }

    fun getDirectoryByCardType(cardType: Card.Type): String {
        return when (cardType) {
            Card.Type.IMPERIUM -> "imperium_cards"
            Card.Type.INTRIGUE -> "intrigue_cards"
            Card.Type.STARTER -> "imperium_cards_starter"
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

    companion object {
        val cardImageUrls = mutableMapOf<Key, List<String>>()
    }
}
