package com.deluxe_viper.androidbluetoothlowenergyproject.Device

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.juul.kable.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import kotlin.math.pow
import kotlin.time.ExperimentalTime
import kotlin.time.TimeMark
import kotlin.time.TimeSource

class DeviceViewModel(application: Application, macAddress: String) :
    AndroidViewModel(application) {

    private val peripheral = viewModelScope.peripheral(macAddress)
    val state = peripheral.state

//    private val deviceTag = DeviceTag(peripheral)


//    @OptIn(ExperimentalTime::class)
//    val data = deviceTag.data2
//        .onStart { startTime = TimeSource.Monotonic.markNow() }
//        .scan(Unit) { accumulator, value ->
//            // TODO: Log these 2 to the console
//            Log.d(TAG, "value in data:  $value")
//        }


    @OptIn(ExperimentalTime::class)
    private var startTime: TimeMark? = null



    fun connect() {
        viewModelScope.connect()

    }

    fun disconnect() {
        viewModelScope.disconnect()
    }

    fun discoverData() {
        if (peripheral.state.value != State.Connected){
            Log.e(TAG, "discoverData: Cannot discover data without BLE connection")
            return
        }
        peripheral.services?.forEach { service ->
            Log.d(TAG, "service of peripheral: ${service.serviceUuid}")
            service.characteristics.forEach { chstic ->
                Log.d(TAG, "Characteristic id: ${chstic.characteristicUuid}")
            }
        }
    }

    private fun CoroutineScope.disconnect() {
        launch {
            Log.d(TAG, "disconnect")
            peripheral.disconnect()
        }
    }

    private fun CoroutineScope.connect() {
        launch {
            Log.d(TAG, "connect")
            try {
                peripheral.connect()


//                if (peripheral.services !== null) {
//                    val gattCharacteristic = characteristicOf(
//                        service = DeviceTag.SERVICE_UUID,
//                        characteristic = "beb5483e-36e1-4688-b7f5-ea07361b26a8"
//                        //    beb5483e-36e1-4688-b7f5-ea07361b26a8
//                    )
//                    peripheral.services!!.forEach { service ->
//                        if (service.serviceUuid.toString() == "4fafc201-1fb5-459e-8fcc-c5c9c331914b") {
//                            service.characteristics.forEach { ch ->
//                                if (ch.characteristicUuid.toString() == "beb5483e-36e1-4688-b7f5-ea07361b26a8") {
//                                    Log.d(TAG, "connect: ${ch.properties}, ${ch.properties.broadcast}, ${ch.properties.indicate}, ${ch.properties.notify}, ${ch.properties.read}")
////                                    deviceTag.data2.collect { data ->
////                                        Log.d(TAG, "connect: gatt data: $data")
////                                    }
//                                    peripheral.observe(gattCharacteristic).collect {
//                                        Log.d(TAG, "Gatt chstic byte array: $it")
//                                    }
//                                }
//                            }
//
//                        }
//                    }
//
//                }
            } catch (e: ConnectionLostException) {
                Log.e(TAG, "Connection Attempt failed")
            }
        }
    }


    companion object {
        const val TAG = "DeviceViewModel"
    }
}

private fun Peripheral.remoteRssi() = flow {
    while (true) {
        val rssi = rssi()
        Log.d("DeviceViewModel", "remoteRssi: $rssi")
        emit(rssi)
        delay(1_000L)
    }
}.catch { cause ->
    if (cause !is NotReadyException) throw cause
}
