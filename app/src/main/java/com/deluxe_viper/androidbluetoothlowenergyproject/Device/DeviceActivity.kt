package com.deluxe_viper.androidbluetoothlowenergyproject.Device

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.juul.kable.*
import com.deluxe_viper.androidbluetoothlowenergyproject.AppTheme
import com.juul.krayon.element.view.ElementViewAdapter
import kotlinx.coroutines.flow.Flow
import com.juul.exercise.annotations.Exercise
import com.juul.exercise.annotations.Extra
import com.deluxe_viper.androidbluetoothlowenergyproject.R
import kotlinx.coroutines.flow.collectLatest

@Exercise(Extra("macAddress", String::class))
class DeviceActivity : ComponentActivity() {

    private val deviceViewModel by viewModels<DeviceViewModel> {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                DeviceViewModel(application, intent.extras?.getString("macAddress") ?: throw IllegalStateException("Mac Address not found in intent")) as T
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                Column(
                    Modifier
                        .background(color = MaterialTheme.colors.background)
                        .fillMaxSize()
                ) {
                    val state by deviceViewModel.state.collectAsState()
                    TopAppBar(
                        title = {
                            Text("Advertisement Example")
                        },
                        actions = {
                            if (state is State.Disconnected) {
                                IconButton(onClick = { deviceViewModel.connect() }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_baseline_bluetooth_connected_24),
                                        contentDescription = "Connect"
                                    )
                                }
                            } else {
                                IconButton(onClick = { deviceViewModel.disconnect() }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_baseline_bluetooth_disabled_24),
                                        contentDescription = "Disconnect"
                                    )
                                }
                            }
                        }
                    )

                    ProvideTextStyle(
                        TextStyle(color = contentColorFor(backgroundColor = MaterialTheme.colors.background))
                    ) {
                        Column(Modifier.padding(20.dp)) {
                            // View model logic
                            val stateName = when(state) {
                                is State.Connecting -> "Connecting"
                                else -> state.javaClass.simpleName.toString()
                            }

                            Text(stateName, fontSize = 18.sp)
                            Spacer(Modifier.size(10.dp))

                            if (state == State.Connected) {
                                deviceViewModel.discoverData()
                            }

                            Button(onClick = {deviceViewModel.writeData()}) { // only works once connected
                                Text(text = "Write Data")
                            }

//                            Log.d(TAG, "onCreate: dataSource: ${deviceViewModel.data}")

//                            val data by deviceViewModel.data.collectAsState(initial = 0)
//                            val data = deviceViewModel.data
//                            Text(text = "Data: $data", fontSize = 15.sp)

//                            val gattData by deviceViewModel.data.collectAsState(initial = 0)
//                            val gattData = deviceViewModel.gattData.collectAsState(initial = emptyList<String>())
//                            Text(text = "Gatt Data: ${gattData}", fontSize = 15.sp)
//                            Text(deviceViewModel.)
//                            AndroidView(
//                                modifier = Modifier.weight(1f),
//                                factory = { context ->
//                                    TextView(context).apply {
//                                        (deviceViewModel.data)
////                                    ElementView(context).apply {
////                                        adapter = ElementViewAdapter(
////                                            dataSource = deviceViewModel.data,
//////                                            updater = ::sensorChart,
////                                        )
////                                    }
//                                    }
//                                },
//                            update = {
//
//                            })
                        }
                    }
                }
            }
        }
    }

//    private fun ElementViewAdapter(dataSource: Flow<Unit>): ElementViewAdapter<Unit> {
//        return ElementViewAdapter(deviceViewModel.data)
//    }

    companion object {
        const val TAG = "DeviceActivity1"
    }
}