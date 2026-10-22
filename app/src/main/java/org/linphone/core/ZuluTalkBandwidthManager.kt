/*
 * Copyright (c) 2026 Thanaaya Technologies.
 *
 * This file is part of ZuluTalk, a fork of linphone-android
 * (see https://www.linphone.org).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.linphone.core

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.annotation.WorkerThread
import org.linphone.LinphoneApplication.Companion.coreContext
import org.linphone.core.tools.Log

/**
 * Sends smaller video on mobile data and other slow or metered links, so the picture gets softer
 * instead of freezing. The download cap is advertised in the SDP, which makes the other phone send
 * smaller video to us as well.
 */
class ZuluTalkBandwidthManager(private val context: Context) {
    companion object {
        private const val TAG = "[ZuluTalk Bandwidth]"

        // kbit/s, both directions, audio included
        private const val LOW_BANDWIDTH = 384
        private const val LOW_VIDEO_SIZE = "qvga"
        private const val FULL_VIDEO_SIZE = "vga"

        // Android's estimate for the current link, below which we treat Wi-Fi as slow too
        private const val SLOW_UPSTREAM_KBPS = 1000
    }

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private var lowBandwidth: Boolean? = null

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
            val low = isLowBandwidth(capabilities)
            coreContext.postOnCoreThread { core ->
                apply(core, low)
            }
        }
    }

    @WorkerThread
    fun start() {
        val capabilities = connectivityManager.getNetworkCapabilities(
            connectivityManager.activeNetwork
        )
        apply(coreContext.core, capabilities == null || isLowBandwidth(capabilities))
        connectivityManager.registerDefaultNetworkCallback(networkCallback)
    }

    @WorkerThread
    fun stop() {
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (iae: IllegalArgumentException) {
            Log.w("$TAG Network callback wasn't registered")
        }
    }

    private fun isLowBandwidth(capabilities: NetworkCapabilities): Boolean {
        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) return true
        // Phone hotspots and data-saver networks report themselves as metered
        if (!capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)) return true
        val upstream = capabilities.linkUpstreamBandwidthKbps
        return upstream in 1 until SLOW_UPSTREAM_KBPS
    }

    @WorkerThread
    private fun apply(core: Core, low: Boolean) {
        // Capabilities change with every signal fluctuation; only renegotiate when the class flips
        if (low == lowBandwidth) return
        lowBandwidth = low

        val bandwidth = if (low) LOW_BANDWIDTH else 0
        core.uploadBandwidth = bandwidth
        core.downloadBandwidth = bandwidth
        core.preferredVideoDefinition = Factory.instance().createVideoDefinitionFromName(
            if (low) LOW_VIDEO_SIZE else FULL_VIDEO_SIZE
        )
        Log.i("$TAG Network is ${if (low) "slow or metered" else "fast"}, video bandwidth set to [$bandwidth]")

        for (call in core.calls) {
            if (call.state == Call.State.StreamsRunning && call.currentParams.isVideoEnabled) {
                Log.i("$TAG Renegotiating video for call [${call.remoteAddress.asStringUriOnly()}]")
                call.update(core.createCallParams(call))
            }
        }
    }
}
