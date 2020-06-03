package com.example.wifisearch

import android.net.wifi.ScanResult

val ScanResult.is24GHz: Boolean
    get() = frequency in 2401..2499