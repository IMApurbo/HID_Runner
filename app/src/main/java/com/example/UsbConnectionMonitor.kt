package com.example

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object UsbConnectionMonitor {

  /**
   * Checks whether the device is currently connected to a host computer or USB power.
   * Leverages Android system sticky broadcasts and root sysfs state.
   */
  suspend fun isUsbConnected(context: Context): Boolean = withContext(Dispatchers.IO) {
    // 1. Check sticky USB_STATE broadcast from Android OS
    try {
      val stickyUsb = context.registerReceiver(null, IntentFilter("android.hardware.usb.action.USB_STATE"))
      if (stickyUsb != null) {
        val connected = stickyUsb.getBooleanExtra("connected", false)
        val configured = stickyUsb.getBooleanExtra("configured", false)
        if (connected || configured) return@withContext true
      }
    } catch (e: Exception) {
      // Ignored
    }

    // 2. Check battery status for USB plugged state
    try {
      val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
      val plugged = batteryIntent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
      if (plugged == BatteryManager.BATTERY_PLUGGED_USB || plugged == BatteryManager.BATTERY_PLUGGED_AC) {
        return@withContext true
      }
    } catch (e: Exception) {
      // Ignored
    }

    // 3. Sysfs check via root if available on the device
    try {
      val process = Runtime.getRuntime().exec(
        arrayOf("su", "-c", "cat /sys/class/udc/*/state /sys/class/android_usb/android0/state /sys/class/power_supply/usb/online 2>/dev/null")
      )
      val output = process.inputStream.bufferedReader().use { it.readText().lowercase() }
      process.waitFor()
      if (output.contains("configured") || output.contains("connected") || output.contains("1")) {
        return@withContext true
      }
    } catch (e: Exception) {
      // Ignored
    }

    false
  }
}
