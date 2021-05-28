package com.msa.slanglabs


import `in`.slanglabs.assistants.base.SlangAssistant
import `in`.slanglabs.assistants.travel.*
import `in`.slanglabs.platform.SlangLocale
import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.msa.slanglabs.data.NetworkDataStore
import com.msa.slanglabs.entities.VoiceAssistantState
import com.msa.slanglabs.entities.response.CityItem
import com.msa.slangtestdrive.base.Action
import com.msa.slangtestdrive.base.NetworkResponse
import com.msa.slangtestdrive.base.SingleLiveEvent
import com.msa.slangtestdrive.third.SlangLabsCommunicator
import com.msa.slangtestdrive.utilities.default
import kotlinx.coroutines.*
import java.util.*
import kotlin.coroutines.CoroutineContext

/**
 * Created by Abhi Muktheeswarar on 10-May-2021
 */

class SlangLabsCommunicatorImpl(
    application: Application,
    assistantId: String,
    apiKey: String,
    override val coroutineContext: CoroutineContext
) :
    SlangLabsCommunicator, CoroutineScope {

    private val tag = "SlangLabsCommunicatorImpl"

    private val voiceAssistantMutableState = MutableLiveData<VoiceAssistantState>().default(
        VoiceAssistantState()
    )
    private val voiceAssistantState: LiveData<VoiceAssistantState> get() = voiceAssistantMutableState

    private val actionsMutable = SingleLiveEvent<Action>()
    override val actions: LiveData<Action> = actionsMutable

    private val networkDataStore by lazy { NetworkDataStore(application, Gson()) }
    private var cities: List<CityItem>? = null
    private var searchUserJourney: SearchUserJourney? = null

    private val logStack = ArrayDeque<String>()

    init {

        Log.d(tag, "Init with: assistantId = $assistantId | apiKey = $apiKey")

        SlangTravelAssistant.setAction(object : SlangTravelAssistant.Action {

            override fun onSearch(
                searchInfo: SearchInfo,
                searchUserJourney: SearchUserJourney
            ): SearchUserJourney.AppState {
                this@SlangLabsCommunicatorImpl.searchUserJourney = searchUserJourney
                Log.d(
                    tag,
                    "onSearch = ${searchInfo.source.city} - ${searchInfo.destination.city} on ${searchInfo.onwardDate}"
                )

                printLog("onSearch\n${searchInfo.source.city} - ${searchInfo.destination.city} on ${searchInfo.onwardDate}")
                validateAndSearch(searchInfo)
                return SearchUserJourney.AppState.WAITING
            }

            override fun onNavigation(
                navigationInfo: NavigationInfo,
                navigationUserJourney: NavigationUserJourney
            ): NavigationUserJourney.AppState? {
                return null
            }

            override fun onAssistantError(assistantError: AssistantError) {
                Log.e(tag, "onAssistantError ${assistantError.description}")
                printLog("onAssistantError\n${assistantError.description}")
            }
        })

        SlangTravelAssistant.setLifecycleObserver(object : SlangTravelAssistant.LifecycleObserver {
            override fun onAssistantInitSuccess() {
                Log.d(tag, "onAssistantInitSuccess")
                printLog("onAssistantInitSuccess")
            }

            override fun onAssistantInitFailure(p0: String?) {
                Log.d(tag, "onAssistantInitFailure")
                printLog("onAssistantInitFailure $p0")
            }

            override fun onAssistantInvoked() {
                Log.d(tag, "onAssistantInvoked")
                printLog("onAssistantInvoked")
                if (cities.isNullOrEmpty()) {
                    downloadAllCities()
                }
            }

            override fun onAssistantClosed(p0: Boolean) {
                Log.d(tag, "onAssistantClosed")
                printLog("onAssistantClosed $p0")
            }

            override fun onAssistantLocaleChanged(p0: Locale?) {
                Log.d(tag, "onAssistantLocaleChanged")
                printLog("onAssistantLocaleChanged $p0")
            }

            override fun onUnrecognisedUtterance(p0: String?): SlangAssistant.Status {
                Log.w(tag, "onUnrecognisedUtterance = $p0")
                printLog("onUnrecognisedUtterance\n$p0")
                return SlangAssistant.Status.FAILURE
            }

            override fun onUtteranceDetected(p0: String?) {
                Log.d(tag, "onUtteranceDetected = $p0")
                printLog("onUtteranceDetected $p0")
            }

            override fun onOnboardingSuccess() {
                Log.d(tag, "onOnboardingSuccess")
                printLog("onOnboardingSuccess")
            }

            override fun onOnboardingFailure() {
                Log.w(tag, "onOnboardingFailure")
                printLog("onOnboardingFailure")
            }
        })

        val requestedLocales = setOf(
            SlangLocale.LOCALE_ENGLISH_IN,
            SlangLocale.LOCALE_HINDI_IN,
        )

        val assistantConfiguration = AssistantConfiguration.Builder()
            .setRequestedLocales(requestedLocales)
            .setAssistantId(assistantId)
            .setAPIKey(apiKey)
            .setDefaultLocale(SlangLocale.LOCALE_ENGLISH_IN)
            .setEnvironment(SlangTravelAssistant.Environment.STAGING)
            .build()

        SlangTravelAssistant.initialize(application, assistantConfiguration)
        SearchUserJourney.disablePreserveContext()
    }

    override fun showTrigger(activity: Activity) {
        Log.d(tag, "showTrigger")
        SlangTravelAssistant.getUI().showTrigger(activity)
        printLog("showTrigger")
    }

    override fun hideTrigger(activity: Activity) {
        SlangTravelAssistant.getUI().hideTrigger(activity)
        printLog("hideTrigger")
    }

    private fun postAction(action: Action) {
        Log.d(tag, "postAction = ${action.javaClass.simpleName}")
        launch(Dispatchers.Main) {
            actionsMutable.value = action
        }
    }

    private fun printLog(text: String) {
        logStack.push(text)
        postAction(SlangLabsCommunicator.SlangLabsAction.PrintLogAction(logStack.toList()))
    }

    private fun downloadAllCities() {
        Log.d(tag, "downloadAllCities")
        launch(Dispatchers.IO + CoroutineExceptionHandler { _, throwable ->
            throwable.printStackTrace()

        }) {

            when (val response = networkDataStore.getAllCities()) {

                is NetworkResponse.Success -> {
                    cities = response.body.sortedBy { it.name }
                    Log.d(tag, "cities loaded = ${cities?.size}")
                    /*cities?.map { it.name }?.forEachIndexed { index, s ->
                        Log.d(tag, "$index $s")
                    }*/

                }
                is NetworkResponse.ServerError -> {
                    val errorMessage = response.body?.message
                        ?: "Something went wrong"
                }
                is NetworkResponse.NetworkError -> {
                    response.error.printStackTrace()
                }
            }
        }
    }

    private fun validateAndSearch(searchInfo: SearchInfo) =
        launch(Dispatchers.IO + CoroutineExceptionHandler { _, throwable ->
            throwable.printStackTrace()
        }) {

            Log.d(tag, "validateAndSearch")

            if (cities.isNullOrEmpty()) {
                launch { downloadAllCities() }.join()
            }

            if (searchInfo.destination.city.isNullOrBlank()) {
                searchUserJourney?.setNeedDestination()
                searchUserJourney?.notifyAppState(SearchUserJourney.AppState.SEARCH_RESULTS)
                Log.e(tag, "destination not available")
                printLog("destination not available")
                return@launch
            }

            val destination = searchCity(searchInfo.destination.city)

            if (destination != null) {
                voiceAssistantMutableState.postValue(voiceAssistantState.value?.copy(destination = destination))
                Log.d(tag, "Destination valid")
                printLog("Destination valid")
            } else {
                searchUserJourney?.setDestinationInvalid()
                searchUserJourney?.notifyAppState(SearchUserJourney.AppState.SEARCH_RESULTS)
                Log.e(tag, "Destination invalid")
                printLog("Destination invalid")
            }

            if (searchInfo.source.city.isNullOrBlank()) {
                searchUserJourney?.setNeedSource()
                searchUserJourney?.notifyAppState(SearchUserJourney.AppState.SEARCH_RESULTS)
                Log.e(tag, "source not available")
                printLog("source not available")
                return@launch
            }

            val source = searchCity(searchInfo.source.city)

            if (source != null) {
                voiceAssistantMutableState.postValue(voiceAssistantState.value?.copy(source = source))
                Log.d(tag, "Source valid")
                printLog("Source valid")
            } else {
                searchUserJourney?.setSourceInvalid()
                searchUserJourney?.notifyAppState(SearchUserJourney.AppState.SEARCH_RESULTS)
                Log.e(tag, "Source invalid")
                printLog("Source invalid")
            }

            val dateOfJourney = searchInfo.onwardDate

            if (dateOfJourney == null) {
                searchUserJourney?.setNeedOnwardDate()
                searchUserJourney?.notifyAppState(SearchUserJourney.AppState.SEARCH_RESULTS)
                Log.e(tag, "received onwardDate invalid")
                printLog("received onwardDate invalid")
                return@launch
            }

            if (dateOfJourney.before(Date())) {
                searchUserJourney?.setOnwardDateInvalid()
                searchUserJourney?.notifyAppState(SearchUserJourney.AppState.SEARCH_RESULTS)
                Log.e(tag, "Date not valid")
                printLog("Date not valid")
                return@launch
            }

            Log.d(tag, "voiceAssistantState = ${voiceAssistantState.value}")
            Log.d(tag, "source = $source")
            Log.d(tag, "destination = $destination")
            Log.d(tag, "dateOfJourney = $dateOfJourney")
            printLog("END")

            if (source != null && destination != null) {

                //Temp fix
                if (source.id == destination.id) {
                    searchUserJourney?.setDestinationInvalid()
                    searchUserJourney?.notifyAppState(SearchUserJourney.AppState.SEARCH_RESULTS)
                    return@launch
                }

                postAction(
                    SlangLabsCommunicator.SlangLabsAction.OpenSearchScreenAction(
                        Pair(source.id, source.name),
                        Pair(destination.id, destination.name), dateOfJourney
                    )
                )
                searchUserJourney?.setSuccess()
                searchUserJourney?.notifyAppState(SearchUserJourney.AppState.SEARCH_RESULTS)
                SearchUserJourney.getContext().clear()
                searchUserJourney = null
            } else {
                Log.e(tag, "Search not success")
            }
        }

    private fun searchCity(name: String): CityItem? {
        val index = cities?.binarySearch {
            String.CASE_INSENSITIVE_ORDER.compare(
                it.name,
                name
            )
        } ?: -1
        return if (index != -1) {
            cities?.get(index)
        } else null
    }
}