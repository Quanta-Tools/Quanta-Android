package tools.quanta.sdk.model

import java.util.Date
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class EventTask(
        val appId: String,
        val userData: String,
        val event: String,
        val revenue: String,
        val addedArguments: String,
        val userId: String,
        @Serializable(with = DateSerializer::class) // Add custom serializer for Date
        val time: Date,
        val abLetters: String? = null
)

// Custom serializer for Date
object DateSerializer : KSerializer<Date> {
        override val descriptor: SerialDescriptor =
                PrimitiveSerialDescriptor("Date", PrimitiveKind.LONG)
        override fun serialize(encoder: Encoder, value: Date) = encoder.encodeLong(value.time)
        override fun deserialize(decoder: Decoder): Date = Date(decoder.decodeLong())
}
