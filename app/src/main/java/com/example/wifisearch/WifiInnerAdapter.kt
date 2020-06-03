package com.example.wifisearch

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.ScanResult
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.hb_layout_network_inner_list_item.view.*

class WifiInnerAdapter(
    private val context: Context
) : RecyclerView.Adapter<WifiInnerAdapter.ViewHolder>() {

    private var networkList = listOf<ScanResult>()
    private val layoutInflater = LayoutInflater.from(context)
    private val colorTextPrimary = ContextCompat.getColor(context, R.color.colorPrimary)
    private val colorTextPrimaryDark = ContextCompat.getColor(context, R.color.colorPrimaryDark)

    private val android.net.wifi.ScanResult.is24GHz: Boolean
        get() = frequency in 2401..2499

    override fun getItemCount() = networkList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = layoutInflater.inflate(R.layout.hb_layout_network_inner_list_item, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        networkList[position].run {
            holder.tvWifi.text = SSID
            holder.tvBssid.text = BSSID
            holder.tvFrequency.text = "$frequency Hz"
            holder.iv5G.isVisible = !is24GHz
            holder.tvWifi.setTextColor(colorTextPrimary)
        }
    }

    fun setData(list: List<ScanResult>) {
        networkList = list
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvWifi: TextView = itemView.tv_wifi
        val tvBssid: TextView = itemView.tv_bssid
        val tvFrequency: TextView = itemView.tv_frequency
        val iv5G: ImageView = itemView.iv_frequency
    }
}