package tools.quanta.sdk.util

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Serializable data class ABExperiment(val name: List<String>, val variants: List<Int>)

// Configure a Json parser instance. ignoreUnknownKeys = true is often helpful.
private val jsonParser = Json { ignoreUnknownKeys = true }
private const val ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"

/**
 * Converts an A/B test JSON string into a string of letters representing chosen variants.
 *
 * For example, if the JSON defines two experiments, and the first resolves to variant 'B' and the
 * second to variant 'A', this function would return "BA".
 *
 * This function relies on `Quanta.id` being set. The result of this function should typically be
 * stored in `Quanta.abLetters` for use by `getAbDict`.
 *
 * @param abJson The JSON string defining the A/B experiments.
 * ```
 *               Example: "[{\"name\":[\"price.1.newname\",\"price.1\"],\"variants\":[90,10]}]"
 * @return
 * ```
 * A string of letters (e.g., "B", "AB") or an empty string if parsing fails or no experiments.
 */
fun getAbLetters(abJson: String, userId: String): String {
    val experiments: List<ABExperiment> =
            try {
                jsonParser.decodeFromString(abJson)
            } catch (e: Exception) {
                // Optionally log e.message for debugging, e.g., println("Error parsing AB JSON for
                // letters: ${e.message}")
                return "" // Mimic Swift's try? behavior by returning empty on error
            }

    val resultLetters = StringBuilder()
    for (exp in experiments) {
        val keyNamePart = exp.name.lastOrNull() ?: ""
        val key = "$userId.$keyNamePart"
        val intValue = stringToNumber(key) // Assumes stringToNumber is in scope from Hash.kt

        var cumulativeVariantPercentage = 0
        for ((index, variantPercentage) in exp.variants.withIndex()) {
            cumulativeVariantPercentage += variantPercentage
            if (cumulativeVariantPercentage > intValue) {
                if (index < ALPHABET.length) {
                    resultLetters.append(ALPHABET[index])
                } else {
                    // This case means the variant index is out of bounds for the ALPHABET.
                    // This might happen if there are more variants than letters in ALPHABET.
                    // Consider logging or specific error handling if this is a possibility.
                    // For now, it matches Swift's behavior of not adding a letter if index is too
                    // high.
                }
                break // Move to the next experiment once a variant is chosen
            }
        }
        // If intValue is >= sum of all variants, no letter is appended for this experiment,
        // matching the Swift logic where the inner loop completes without a break and no letter is
        // added.
    }
    return resultLetters.toString()
}

/**
 * Converts an A/B test JSON string into a dictionary mapping experiment names to their chosen
 * variant letter.
 *
 * This function relies on `Quanta.abLetters` having been previously populated by `getAbLetters`.
 * The `abJson` parameter here should ideally be the same one used to generate `Quanta.abLetters` to
 * ensure consistency.
 *
 * @param abJson The JSON string defining the A/B experiments.
 * @return A map where keys are lowercase experiment names and values are their assigned variant
 * letters.
 * ```
 *         Example: {"price.1": "B", "price.1.newname": "B"}
 * ```
 */
fun getAbDict(abJson: String, abLetters: String): Map<String, String> {
    val nameToLetterMap = mutableMapOf<String, String>()
    val experiments: List<ABExperiment> =
            try {
                jsonParser.decodeFromString(abJson)
            } catch (e: Exception) {
                // Optionally log e.message for debugging, e.g., println("Error parsing AB JSON for
                // dict: ${e.message}")
                return nameToLetterMap // Return empty map on error
            }

    for ((experimentIndex, experiment) in experiments.withIndex()) {
        if (experimentIndex < abLetters.length) {
            val assignedLetter = abLetters[experimentIndex].toString()
            for (name in experiment.name) {
                nameToLetterMap[name.lowercase()] = assignedLetter
            }
        } else {
            // Not enough letters in Quanta.abLetters for all experiments.
            // This might indicate that Quanta.abLetters is stale, from a different abJson,
            // or an issue in the letter generation logic for some experiments.
            // Stop processing further experiments to avoid incorrect mappings.
            break
        }
    }
    return nameToLetterMap
}
