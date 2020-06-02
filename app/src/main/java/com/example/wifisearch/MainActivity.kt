package com.example.wifisearch

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.net.Uri
import android.net.wifi.WifiManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions

/**
 * 扫描并显示周边Wi-Fi列表
 */
@RuntimePermissions
class MainActivity : AppCompatActivity() {

    private val tag = "MainActivity"

    private lateinit var wifiManager: WifiManager
    private lateinit var locationManager: LocationManager

    private val wifiScanReceiver: BroadcastReceiver by lazy {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.hasExtra(WifiManager.EXTRA_RESULTS_UPDATED)) {
                    fetchScanResult()
//                    val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
//                    if (success) {
//
//                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        registerWifiInfoReceiver()

        //todo 读取Wi-Fi列表，并根据列表中是否有相同的两个SSID，不同频段，来界定是否是双频合一
        checkIfLocationIsOn(
            onLocationTurnedOn = {
                Toast.makeText(this, "开始扫描Wi-Fi网络，妹30s刷新一次", Toast.LENGTH_SHORT).show()
                lifecycleScope.launch {
                    while (isActive) {
                        scanWifiWithPermissionCheck()
                        delay(30000L)
                    }
                }
            },
            onLocationTurnedOff = {
                Toast.makeText(this, "请打开位置服务", Toast.LENGTH_SHORT).show()
            }
        )
    }

    @NeedsPermission(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    fun scanWifi() {
        Log.d(tag, "ChooseNetworkActivity, scanWifi")
        doScanWifi {
            Toast.makeText(this, "扫描WiFi需要开启位置信息", Toast.LENGTH_SHORT).show()
            startLocationSettingActivity()
        }
    }

    private fun doScanWifi(onLocationTurnedOff: () -> Unit) {
        checkIfLocationIsOn(
            onLocationTurnedOff, {
            wifiManager.startScan()
        })
    }

    private fun startLocationSettingActivity() {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }

    private fun startPermissionSettingActivity() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.parse("package:$packageName")
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }


    private fun registerWifiInfoReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        registerReceiver(wifiScanReceiver, intentFilter)
    }

    private fun fetchScanResult() {
        wifiManager.scanResults.filter {
            it.SSID.isNotBlank()
        }.sortedByDescending { it.level }.forEach {
            Log.d(tag, "$it")
        }
    }

    private fun checkIfLocationIsOn(
        onLocationTurnedOff: () -> Unit,
        onLocationTurnedOn: () -> Unit
    ) {
        if (!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            onLocationTurnedOff()
        } else {
            onLocationTurnedOn()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(wifiScanReceiver)
    }
}