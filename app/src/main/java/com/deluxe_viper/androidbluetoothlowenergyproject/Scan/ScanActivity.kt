package com.deluxe_viper.androidbluetoothlowenergyproject.Scan

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.BLUETOOTH_CONNECT
import android.Manifest.permission.BLUETOOTH_SCAN
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothAdapter.*
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.*
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deluxe_viper.androidbluetoothlowenergyproject.AppTheme
import com.deluxe_viper.androidbluetoothlowenergyproject.Device.DeviceActivity
import com.deluxe_viper.androidbluetoothlowenergyproject.ScanStatus
import com.deluxe_viper.androidbluetoothlowenergyproject.ScanViewModel
import com.deluxe_viper.androidbluetoothlowenergyproject.icons.BluetoothDisabled
import com.deluxe_viper.androidbluetoothlowenergyproject.icons.LocationDisabled
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.juul.kable.Advertisement
import com.juul.tuulbox.coroutines.flow.broadcastReceiverFlow
import kotlinx.coroutines.flow.map
import com.deluxe_viper.androidbluetoothlowenergyproject.enableBluetooth
import com.deluxe_viper.androidbluetoothlowenergyproject.openAppDetails
//import com.deluxe_viper.androidbluetoothlowenergyproject.Device.DeviceActivityIntent

class ScanActivity : ComponentActivity() {

    private val isBluetoothEnabled = broadcastReceiverFlow(IntentFilter(ACTION_STATE_CHANGED))
        .map { intent -> intent.getIsBluetoothEnabled() }
    private val viewModel by viewModels<ScanViewModel>()

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                val isBluetoothEnabled = isBluetoothEnabled
                    .collectAsState(initial = BluetoothAdapter.getDefaultAdapter().isEnabled)
                    .value

                Column(Modifier.background(color = MaterialTheme.colors.background)) {
                    AppBar(viewModel, isBluetoothEnabled)

                    Box(Modifier.weight(1f)) {
                        ProvideTextStyle(
                            TextStyle(color = contentColorFor(backgroundColor = MaterialTheme.colors.background))
                        ) {
                            val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                listOf(BLUETOOTH_SCAN, BLUETOOTH_CONNECT, ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION)
                            } else {
                                listOf(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION)
                            }
                            val permissionsState =
                                rememberMultiplePermissionsState(permissions = permissions)

                            var didAskForPermission by remember { mutableStateOf(false) }
                            if (!didAskForPermission) {
                                didAskForPermission = true
                                SideEffect {
                                    permissionsState.launchMultiplePermissionRequest()
                                }
                            }

                            if (permissionsState.allPermissionsGranted) {
                                if (isBluetoothEnabled) {
                                    val advertisements =
                                        viewModel.advertisements.collectAsState().value
                                    AdvertisementsList(advertisements, ::onAdvertisementClicked)
                                } else {
                                    BluetoothDisabled(::enableBluetooth)
                                }
                            } else {
                                if (permissionsState.shouldShowRationale) {
                                    BluetoothPermissionsNotGranted(permissions = permissionsState)
                                } else {
                                    BluetoothPermissionsNotAvailable(::openAppDetails)
                                }
                            }

                            StatusSnackbar(viewModel)
                        }
                    }
                }
            }
        }
    }

    private fun onAdvertisementClicked(advertisement: Advertisement, context: Context) {
        viewModel.stop()
        val intent = Intent(context, DeviceActivity::class.java)
        intent.putExtra("macAddress", advertisement.address)
        context.startActivity(intent)
    }

    override fun onPause() {
        super.onPause()
        viewModel.stop()
    }
}

@Composable
private fun AppBar(viewModel: ScanViewModel, isBluetoothEnabled: Boolean) {
    val status = viewModel.status.collectAsState().value

    TopAppBar(
        title = {
            Text("BLE Example")
        },
        actions = {
            if (isBluetoothEnabled) {
                if (status !is ScanStatus.Scanning) {
                    IconButton(onClick = viewModel::startScan) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                    }
                }
                IconButton(onClick = viewModel::clear) {
                    Icon(Icons.Filled.Delete, contentDescription = "Clear")
                }
            }
        }
    )
}

@Composable
private fun BoxScope.StatusSnackbar(viewModel: ScanViewModel) {
    val status = viewModel.status.collectAsState().value

    if (status !is ScanStatus.Stopped) {
        val text = when (status) {
            is ScanStatus.Scanning -> "Scanning"
            is ScanStatus.Stopped -> "Idle"
            is ScanStatus.Failed -> "Error: ${status.message}"
            else -> {
                throw IllegalStateException("Error. ScanStatus not found.")
            }
        }
        Snackbar(
            Modifier
                .align(Alignment.BottomCenter)
                .padding(10.dp)
        ) {
            Text(text, style = MaterialTheme.typography.body1)
        }
    }
}

@Composable
private fun ActionRequired(
    icon: ImageVector,
    contentDescription: String?,
    description: String,
    buttonText: String,
    onClick: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            modifier = Modifier.size(150.dp),
            tint = contentColorFor(backgroundColor = MaterialTheme.colors.background),
            imageVector = icon,
            contentDescription = contentDescription,
        )
        Spacer(Modifier.size(8.dp))
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .align(CenterHorizontally),
            textAlign = TextAlign.Center,
            text = description
        )
        Spacer(Modifier.size(15.dp))
        Button(onClick) {
            Text(buttonText)
        }
    }
}

@Composable
private fun BluetoothDisabled(enableAction: () -> Unit) {
    ActionRequired(
        icon = Icons.Filled.BluetoothDisabled,
        contentDescription = "Bluetooth Disabled",
        description = "Bluetooth is disabled",
        buttonText = "Enable",
        onClick = enableAction
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun BluetoothPermissionsNotGranted(permissions: MultiplePermissionsState) {
    ActionRequired(
        icon = Icons.Filled.LocationDisabled,
        contentDescription = "Bluetooth permissions required",
        description = "Bluetooth permissions are required for scanning",
        buttonText = "Continue",
        onClick = permissions::launchMultiplePermissionRequest
    )
}

@Composable
private fun BluetoothPermissionsNotAvailable(openSettingsAction: () -> Unit) {
    ActionRequired(
        icon = Icons.Filled.Warning,
        contentDescription = "Bluetooth permissions required",
        description = "Bluetooth permission denied. Please, grant access on the Settings screen.",
        buttonText = "Open Settings",
        onClick = openSettingsAction,
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun LocationPermissionsNotGranted(permissions: MultiplePermissionsState) {
    ActionRequired(
        icon = Icons.Filled.LocationDisabled,
        contentDescription = "Location permissions required",
        description = "Location permissions are required for scanning",
        buttonText = "Continue",
        onClick = permissions::launchMultiplePermissionRequest
    )
}

@Composable
private fun LocationPermissionsNotAvailable(openSettingsAction: () -> Unit) {
    ActionRequired(
        icon = Icons.Filled.LocationDisabled,
        contentDescription = "Location permissions required",
        description = "Location permissions are required for scanning",
        buttonText = "Continue",
        onClick = openSettingsAction
    )
}

@Composable
private fun AdvertisementsList(
    advertisements: List<Advertisement>,
    onRowClick: (Advertisement, Context) -> Unit
) {
    val mContext = LocalContext.current

    LazyColumn {
        items(advertisements.size) { index ->
            val advertisement = advertisements[index]
            AdvertisementRow(advertisement) { onRowClick(advertisement, mContext) }
        }
    }
}

@Composable
private fun AdvertisementRow(advertisement: Advertisement, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .clickable(onClick = onClick)
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                fontSize = 22.sp,
                text = advertisement.name ?: "Unknown"
            )
            Text(advertisement.address)
        }

        Text(
            modifier = Modifier.align(CenterVertically),
            text = "${advertisement.rssi} dBm"
        )
    }
}

private fun Intent.getIsBluetoothEnabled(): Boolean = when (getIntExtra(EXTRA_STATE, ERROR)) {
    STATE_TURNING_ON, STATE_ON, STATE_CONNECTING, STATE_CONNECTED, STATE_DISCONNECTED, STATE_DISCONNECTING -> true
    else -> false // STATE_TURNING_OFF, STATE_OFF
}