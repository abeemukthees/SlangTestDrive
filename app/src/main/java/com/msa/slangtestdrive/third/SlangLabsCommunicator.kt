package com.msa.slangtestdrive.third

import android.app.Activity
import android.app.Application
import androidx.lifecycle.LiveData
import com.msa.slangtestdrive.base.Action
import com.msa.slangtestdrive.base.NavigateAction
import kotlinx.coroutines.Dispatchers
import java.util.*
import kotlin.reflect.full.primaryConstructor

/**
 * Created by Abhi Muktheeswarar on 10-May-2021
 */

interface SlangLabsCommunicator {

    companion object {

        @Volatile
        private var INSTANCE: SlangLabsCommunicator? = null

        private const val slangLabsCommunicatorImpl =
            "com.msa.slanglabs.SlangLabsCommunicatorImpl"
        private const val assistantId = ""
        private const val apiKey = ""

        private const val tag = "SlangLabsCommunicator"

        @JvmStatic
        fun getInstance(
            application: Application
        ): SlangLabsCommunicator =
            INSTANCE ?: synchronized(this) {
                INSTANCE
                    ?: (Class.forName(slangLabsCommunicatorImpl).kotlin.primaryConstructor?.call(
                        application,
                        assistantId,
                        apiKey,
                        Dispatchers.Default
                    ) as SlangLabsCommunicator).also { INSTANCE = it }
            }
    }

    val actions: LiveData<Action>

    fun showTrigger(activity: Activity)

    fun hideTrigger(activity: Activity)

    sealed class SlangLabsAction : Action {

        data class OpenSearchScreenAction(
            val sourceIdName: Pair<Int, String>,
            val destinationIdName: Pair<Int, String>,
            val dateOfJourney: Date
        ) : SlangLabsAction(), NavigateAction

        data class PrintLogAction(val textList: List<String>) : SlangLabsAction()
    }
}
