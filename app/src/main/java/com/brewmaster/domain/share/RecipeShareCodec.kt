package com.brewmaster.domain.share

import android.net.Uri
import android.util.Base64
import com.brewmaster.domain.model.GrindSize
import com.brewmaster.domain.model.PersonalRecipe
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Encodes / decodes a [PersonalRecipe] to a shareable deep link so recipes can be
 * sent over any app (chat, email, notes) and re-imported.
 *
 * Format: brewmaster://recipe?d=<base64-url-safe JSON>
 */
object RecipeShareCodec {

    const val SCHEME = "brewmaster"
    const val HOST = "recipe"
    private const val PARAM = "d"

    private val json = Json { ignoreUnknownKeys = true }

    @Serializable
    private data class SharedRecipe(
        val beanName: String,
        val techniqueId: String,
        val processId: Int,
        val grindSize: String,
        val ratio: Double,
        val coffeeWeight: Double,
        val isIce: Boolean,
        val iceWeight: Double? = null,
        val notes: String? = null
    )

    /** Builds the brewmaster://recipe?d=... deep link for a recipe. */
    fun encode(recipe: PersonalRecipe): String {
        val dto = SharedRecipe(
            beanName = recipe.beanName,
            techniqueId = recipe.techniqueId,
            processId = recipe.processId,
            grindSize = recipe.grindSize.name,
            ratio = recipe.ratio,
            coffeeWeight = recipe.coffeeWeight,
            isIce = recipe.isIce,
            iceWeight = recipe.iceWeight,
            notes = recipe.notes
        )
        val payload = json.encodeToString(dto)
        val code = Base64.encodeToString(
            payload.toByteArray(Charsets.UTF_8),
            Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
        )
        return "$SCHEME://$HOST?$PARAM=$code"
    }

    /** Parses a brewmaster://recipe?d=... link back into a recipe, or null if invalid. */
    fun decode(link: String): PersonalRecipe? {
        return try {
            val uri = Uri.parse(link)
            if (uri.scheme != SCHEME || uri.host != HOST) return null
            val code = uri.getQueryParameter(PARAM) ?: return null
            val payload = String(
                Base64.decode(code, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING),
                Charsets.UTF_8
            )
            val dto = json.decodeFromString<SharedRecipe>(payload)
            PersonalRecipe(
                beanName = dto.beanName,
                techniqueId = dto.techniqueId,
                processId = dto.processId,
                grindSize = runCatching { GrindSize.valueOf(dto.grindSize) }.getOrDefault(GrindSize.MEDIUM),
                ratio = dto.ratio,
                coffeeWeight = dto.coffeeWeight,
                isIce = dto.isIce,
                iceWeight = dto.iceWeight,
                notes = dto.notes,
                createdAt = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            null
        }
    }
}
