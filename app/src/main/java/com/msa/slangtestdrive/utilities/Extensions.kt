package com.msa.slangtestdrive.utilities

import androidx.lifecycle.MutableLiveData

/**
 * Created by Abhi Muktheeswarar on 27-May-2021
 */

fun <T : Any?> MutableLiveData<T>.default(initialValue: T) = apply { setValue(initialValue) }