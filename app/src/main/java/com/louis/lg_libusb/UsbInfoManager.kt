package com.louis.lg_libusb

/**
 * Created by louisgeek on 2022/5/11.
 */
object UsbInfoManager {
    init {
        System.loadLibrary("usbinfo")
    }

    external fun getDeviceInfo(fileDescriptor: Int): Int
}