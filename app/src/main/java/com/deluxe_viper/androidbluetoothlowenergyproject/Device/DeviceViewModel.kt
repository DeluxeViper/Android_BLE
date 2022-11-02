package com.deluxe_viper.androidbluetoothlowenergyproject.Device

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.juul.kable.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.pow
import kotlin.time.ExperimentalTime
import kotlin.time.TimeMark
import com.juul.kable.DiscoveredService
import kotlin.time.TimeSource

private val DISCONNECT_TIMEOUT = TimeUnit.SECONDS.toMillis(5);

sealed class ViewState {
    object Connecting : ViewState()

    object Connected : ViewState()
//    data class Connected(
//        val rssi: String,
////        val gyro: GyroState
//    ) : ViewState()

    object Disconnecting : ViewState()

    object Disconnected : ViewState()
}

val ViewState.label: CharSequence
    get() = when (this) {
        ViewState.Connecting -> "Connecting"
        is ViewState.Connected -> "Connected"
        ViewState.Disconnecting -> "Disconnecting"
        ViewState.Disconnected -> "Disconnected"
    }

class DeviceViewModel(application: Application, macAddress: String) :
    AndroidViewModel(application) {

    private val peripheral = viewModelScope.peripheral(macAddress)
    private val deviceTag = DeviceTag(peripheral)
    private val connectionAttempt = AtomicInteger()

    private val periodProgress = AtomicInteger()

    @OptIn(ExperimentalTime::class)
    val data = deviceTag.data2
        .onStart { startTime = TimeSource.Monotonic.markNow() }
        .scan(Unit) { accumulator, value ->
            // TODO: Log these 2 to the console
            Log.d(TAG, "value in data:  $value")
        }


    @OptIn(ExperimentalTime::class)
    private var startTime: TimeMark? = null

    init {
        viewModelScope.enableAutoReconnect()
        viewModelScope.connect()
    }

    private fun CoroutineScope.enableAutoReconnect() {
        peripheral.state
            .onEach {
                val timeMillis = backoff(
                    base = 500L,
                    multiplier = 2f,
                    retry = connectionAttempt.getAndIncrement()
                )
                Log.i(TAG, "Wait $timeMillis ms to reconnect...")
                delay(timeMillis)
                connect()

                if (peripheral.services !== null) {
                    deviceTag.readCharacteristic()

                }
            }.launchIn(this)
    }

    private fun CoroutineScope.connect() {
        connectionAttempt.incrementAndGet()
        launch {
            Log.d(TAG, "connect ")
            try {
                Log.d(TAG, "connect: peripheral:: $peripheral + ${peripheral.name}")
                peripheral
                Log.d(TAG, "connect: peripheral services: ${peripheral.services}")
                peripheral.services?.forEach { service ->
//                    Log.d(TAG, "services of peripheral: ${service.characteristics}")
                    service.characteristics.forEach { chstic ->
                        Log.d(
                            TAG,
                            "connect: chstics of peripheral, service id: ${service.serviceUuid}, chstic id: ${chstic.characteristicUuid}"
                        )
                    }
                }
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

                // enable/disable stuff
            } catch (e: ConnectionLostException) {
                Log.e(TAG, "Connection Attempt failed")
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val viewState: Flow<ViewState> = peripheral.state.flatMapLatest { state ->
        when (state) {
            is State.Connecting -> flowOf(ViewState.Connecting)
            State.Connected -> flowOf(ViewState.Connected)
//            State.Connected -> flowOf(ViewState.Connected(peripheral.remoteRssi())
            State.Disconnecting -> flowOf(ViewState.Disconnecting)
            is State.Disconnected -> flowOf(ViewState.Disconnected)
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

/**
 * Exponential backoff using the following formula:
 *
 * ```
 * delay = base * multiplier ^ retry
 * ```
 *
 * For example (using `base = 100` and `multiplier = 2`):
 *
 * | retry | delay |
 * |-------|-------|
 * |   1   |   100 |
 * |   2   |   200 |
 * |   3   |   400 |
 * |   4   |   800 |
 * |   5   |  1600 |
 * |  ...  |   ... |
 *
 * Inspired by:
 * [Exponential Backoff And Jitter](https://aws.amazon.com/blogs/architecture/exponential-backoff-and-jitter/)
 *
 * @return Backoff delay (in units matching [base] units, e.g. if [base] units are milliseconds then returned delay will be milliseconds).
 */
private fun backoff(
    base: Long,
    multiplier: Float,
    retry: Int,
): Long = (base * multiplier.pow(retry - 1)).toLong()