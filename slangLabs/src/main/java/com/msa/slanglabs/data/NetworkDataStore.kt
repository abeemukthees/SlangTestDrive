package com.msa.slanglabs.data

import android.content.Context
import com.google.gson.Gson
import com.msa.slanglabs.entities.response.CityItem
import com.msa.slangtestdrive.base.NetworkResponse
import java.io.IOException
import java.nio.charset.Charset

/**
 * Created by Abhi Muktheeswarar on 19-May-2021
 */

class NetworkDataStore(private val context: Context, private val gson: Gson) {

    @Suppress("SameParameterValue")
    fun getAllCities(): NetworkResponse<List<CityItem>, Error> {
        val json = loadJSONFromAsset("cities.json")
        return if (json is String) {
            val response = gson.fromJson(json, Array<CityItem>::class.java)
            NetworkResponse.Success(response.toList())

        } else {
            NetworkResponse.NetworkError(json as IOException)
        }
    }

    private fun loadJSONFromAsset(fileName: String): Any {
        var json: String?
        try {
            val inputStream = context.assets.open(fileName)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            json = String(buffer, Charset.forName("UTF-8"))
        } catch (ex: IOException) {
            ex.printStackTrace()
            return ex
        }

        return json
    }
}