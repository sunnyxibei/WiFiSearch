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
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
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
    private lateinit var wifiAdapter: WifiAdapter

    private val wifiScanReceiver: BroadcastReceiver by lazy {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.hasExtra(WifiManager.EXTRA_RESULTS_UPDATED)) {
                    fetchScanResult()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        wifiAdapter = WifiAdapter(this) {
            val intent = Intent(this, WifiListActivity::class.java)
            intent.putExtra("ssid", it.first)
            intent.putExtra("wifi_list", it.second.toTypedArray())
            startActivity(intent)
        }
        wifi_list.layoutManager = LinearLayoutManager(this)
        wifi_list.adapter = wifiAdapter

        registerWifiInfoReceiver()
        checkIfLocationIsOn(
            onLocationTurnedOn = {
                Toast.makeText(
                    this,
                    "开始扫描Wi-Fi网络，每分钟刷新一次，点击Wi-Fi条目可以查看是否是双频合一路由。",
                    Toast.LENGTH_LONG
                ).show()
                lifecycleScope.launch {
                    while (isActive) {
                        scanWifiWithPermissionCheck()
                        delay(60000L)
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
        val scanResultMap = wifiManager.scanResults.filter {
            it.SSID.isNotBlank()
        }.groupBy { it.SSID }
        wifiAdapter.setData(scanResultMap)
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // NOTE: delegate the permission handling to generated function
        onRequestPermissionsResult(requestCode, grantResults)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(wifiScanReceiver)
    }
}