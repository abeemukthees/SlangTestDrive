package com.msa.slanglabs.entities

import com.msa.slanglabs.entities.response.CityItem
import java.util.*

/**
 * Created by Abhi Muktheeswarar on 19-May-2021
 */

data class VoiceAssistantState(
    val isInitializationCompleted: Boolean = false,
    val loading: Boolean = false,
    val source: CityItem? = null,
    val destination: CityItem? = null,
    val dateOfJourney: Date? = null
)
