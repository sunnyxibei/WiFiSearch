package com.example.wifisearch

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.net.wifi.ScanResult
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.hb_layout_network_list_item.view.*


class WifiAdapter(
    private val context: Context,
    private val onNetworkClickListener: (Pair<String, List<ScanResult>>) -> Unit
) : RecyclerView.Adapter<WifiAdapter.ViewHolder>() {

    private var networkMap = mapOf<String, List<ScanResult>>()
    private val layoutInflater = LayoutInflater.from(context)
    private val colorTextPrimary = ContextCompat.getColor(context, R.color.colorPrimary)

    private val ScanResult.is24GHz: Boolean
        get() = frequency in 2401..2499

    override fun getItemCount() = networkMap.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = layoutInflater.inflate(R.layout.hb_layout_network_list_item, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        networkMap.toList()[position].run {
            holder.tvWifi.setTextColor(Color.DKGRAY)
            holder.tvWifi.text = first
            if (second.isNotEmpty()) {
                var isDoubleFrequency = false
                if (second.size == 1) {
                    if (second[0].is24GHz) {
                        //单频2.4GHz
                        holder.iv5G.setImageResource(R.mipmap.single_24g_frequency)
                    } else {
                        //单频5GHz
                        holder.iv5G.setImageResource(R.drawable.ic_single_5g_frequency)
                    }
                } else {
                    val has24Ghz = second.find { it.is24GHz } != null
                    val has5GHz = second.find { !it.is24GHz } != null
                    isDoubleFrequency = has24Ghz && has5GHz
                    if (has24Ghz && has5GHz) {
                        //2.4GHz & 5GHz 双频
                        holder.tvWifi.setTextColor(colorTextPrimary)
                        holder.iv5G.setImageResource(R.drawable.ic_double_frequency)
                    } else if (has24Ghz) {
                        //单频2.4GHz
                        holder.iv5G.setImageResource(R.mipmap.single_24g_frequency)
                    } else if (has5GHz) {
                        //单频5GHz
                        holder.iv5G.setImageResource(R.drawable.ic_single_5g_frequency)
                    }
                }
                holder.itemView.setOnClickListener {
                    if (isDoubleFrequency) {
                        Toast.makeText(context, "您选择的是双频合一Wi-Fi路由", Toast.LENGTH_SHORT).show()
                    }
                    onNetworkClickListener(this)
                }
            }
        }
    }

    fun setData(list: Map<String, List<ScanResult>>) {
        networkMap = list
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvWifi: TextView = itemView.tv_wifi
        val iv5G: ImageView = itemView.iv_frequency
    }
}