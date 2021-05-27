package com.msa.slangtestdrive

import android.app.Application
import com.msa.slangtestdrive.third.SlangLabsCommunicator

/**
 * Created by Abhi Muktheeswarar on 17-May-2021
 */

class SlangTestDriveApp : Application() {

    override fun onCreate() {
        super.onCreate()
        SlangLabsCommunicator.getInstance(this)
    }
}