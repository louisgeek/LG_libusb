package com.louis.lg_libusb

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

/**
 * Created by louisgeek on 2022/5/11.
 */
class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var usbManager: UsbManager
    private lateinit var usbPermissionPI: PendingIntent
    private val ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        PermissionsManager.init(this) {
            //
            requestUsbPermission()
        }

        usbManager = this.getSystemService(Context.USB_SERVICE) as UsbManager
        usbPermissionPI = PendingIntent.getBroadcast(this, 0, Intent(ACTION_USB_PERMISSION), 0)

        val intentFilter = IntentFilter()
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        intentFilter.addAction(ACTION_USB_PERMISSION)
        registerReceiver(usbBroadcastReceiver, intentFilter)


    }

    private fun requestUsbPermission() {
        Log.e(TAG, "requestUsbPermission: ")
        val deviceMap = usbManager.deviceList
        if (deviceMap.values.isNotEmpty()) {
            val device = deviceMap.values.elementAt(0)
            if (usbManager.hasPermission(device)) {
                openUsbDevice(device)
            } else {
                usbManager.requestPermission(device, usbPermissionPI)
            }
        }
    }


    private val usbBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                    val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                    if (device != null) {
//                        usbManager.requestPermission(device, usbPermissionPendingIntent)
                    }
                }
                UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                    val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                    if (device != null) {

                    }
//                    lastAuthorisedDevice = null
                }
                ACTION_USB_PERMISSION -> {
                    synchronized(this) {
                        val usbDevice =
                            intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                            if (usbDevice != null) {
//                                lastAuthorisedDevice = device
                                openUsbDevice(usbDevice)
                            }
                        } else {

                        }
                    }
                }
            }

        }
    }

    private fun openUsbDevice(usbDevice: UsbDevice) {
        Log.e(TAG, "openUsbDevice: ")
        val usbDeviceConnection = usbManager.openDevice(usbDevice)
        val fileDescriptor = usbDeviceConnection.fileDescriptor
        UsbInfoManager.getDeviceInfo(fileDescriptor)
    }

    override fun onDestroy() {
        unregisterReceiver(usbBroadcastReceiver)
        super.onDestroy()
    }
}