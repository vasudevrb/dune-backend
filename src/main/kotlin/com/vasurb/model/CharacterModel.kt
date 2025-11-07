package com.vasurb.model

import com.vasurb.util.CharacterUrlRetriever

data class CharacterModel(
    val name: String,
    val urls: List<String>,
    val avatarUrl: String,
) {
    companion object {
        val MUAD_DIB = get(PlayableCharacter.MUAD_DIB)

        fun get(c: PlayableCharacter): CharacterModel {
            return CharacterModel(
                c.readableName,
                CharacterUrlRetriever.characterImageUrls.getValue(c),
                CharacterUrlRetriever.avatarImageUrl.getValue(c)
            )
        }
    }
}
