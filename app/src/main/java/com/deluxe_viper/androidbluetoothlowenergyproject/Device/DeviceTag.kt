package com.deluxe_viper.androidbluetoothlowenergyproject.Device

import android.util.Log
import com.juul.kable.DiscoveredService
import com.juul.kable.Peripheral
import com.juul.kable.characteristicOf
import com.juul.tuulbox.encoding.toHexString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map


//private const val SERVICE_UUID = "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
//
//private val gattCharacteristic = characteristicOf(
//    service = SERVICE_UUID,
//    characteristic = "BEB5483E-36E1-4688-B7F5-EA07361B26A8"
//                //    beb5483e-36e1-4688-b7f5-ea07361b26a8
//)

class DeviceTag(private val peripheral: Peripheral) {
//    val data2: Flow<Int> = peripheral.observe(gattCharacteristic)
//        .map {
//            // TODO: What kind of values can we print here?
//            Log.d(TAG, "Characteristic Values: $it ")
//        }
//
//    val data: List<DiscoveredService>? = peripheral.services

    suspend fun readCharacteristic(): Int {
        val value = peripheral.read(gattCharacteristic)
        Log.d(TAG, "readCharacteristic = ${value.toHexString()}")

        return value[0] * 1
    }

    companion object {
        const val TAG = "Device TAG"
        const val SERVICE_UUID = "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
        val gattCharacteristic = characteristicOf(
            service = SERVICE_UUID,
            characteristic = "beb5483e-36e1-4688-b7f5-ea07361b26a8"
            //    beb5483e-36e1-4688-b7f5-ea07361b26a8
            // HM-10 Service: 0000ffe0-0000-1000-8000-00805f9b34fb
            // HM-10 Characteristic: 0000ffe1-0000-1000-8000-00805f9b34fb
            //
        )
    }
}