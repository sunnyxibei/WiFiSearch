package com.example.wifisearch

import android.net.wifi.ScanResult
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_wifi_list.*

class WifiListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wifi_list)

        toolbar.title = intent.getStringExtra("ssid").orEmpty()
        wifi_inner_list.layoutManager = LinearLayoutManager(this)
        wifi_inner_list.adapter = WifiInnerAdapter(this).apply {
            intent.getParcelableArrayExtra("wifi_list")?.let { it ->
                it.map { parcelable ->
                    parcelable as ScanResult
                }.let {
                    setData(it)
                }
            }
        }
    }

}