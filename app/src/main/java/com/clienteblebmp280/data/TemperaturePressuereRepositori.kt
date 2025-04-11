package com.clienteblebmp280.data

import com.clienteblebmp280.data.ble.BLTClient
import javax.inject.Inject

class TemperaturePressuereRepositori @Inject constructor(
    private val client:BLTClient
) {


    suspend fun getData(): TempPressuereModel?{

        return null
    }
}