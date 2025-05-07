package tools.quanta.sdk.model

import java.util.Date

data class EventTask(
    val appId: String,
    val userData: String,
    val event: String,
    val revenue: String,
    val addedArguments: String,
    val time: Date,
    val abLetters: String? = null
)