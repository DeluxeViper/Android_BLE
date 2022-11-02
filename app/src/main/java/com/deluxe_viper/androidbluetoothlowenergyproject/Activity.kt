package com.deluxe_viper.androidbluetoothlowenergyproject

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.net.Uri
import android.provider.Settings

object RequestCode {
    const val EnableBluetooth = 55001
}

@SuppressLint("MissingPermission")
// Suppressed missing permission warning because this function is only called when the warning was checked
fun Activity.enableBluetooth() {
    startActivityForResult(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), RequestCode.EnableBluetooth)
}

//fun Activity.enableLocation() {
//    startActivityForResult(Intent(Manifest.permission.ACCESS_FINE_LOCATION))
//}

fun Activity.openAppDetails() {
    startActivity(Intent().apply {
        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        addCategory(Intent.CATEGORY_DEFAULT)
        data = Uri.parse("package:$packageName")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
    })
}