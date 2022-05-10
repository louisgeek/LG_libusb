#include <jni.h>
//
#include <android/log.h>
//
#include "libusb.h"

#define LOG_TAG "usbinfo"

#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

extern "C"
JNIEXPORT jint JNICALL
Java_com_louis_lg_1libusb_UsbInfoManager_getDeviceInfo(JNIEnv *env, jobject thiz,
                                                       jint fileDescriptor) {
    libusb_context *ctx;
    libusb_device_handle *devh;
//    libusb_set_option(&ctx, LIBUSB_OPTION_NO_DEVICE_DISCOVERY, NULL);
    int re = libusb_set_option(NULL, LIBUSB_OPTION_NO_DEVICE_DISCOVERY, NULL);
    LOGE("libusb_set_option LIBUSB_OPTION_NO_DEVICE_DISCOVERY: %d", re);
    re = libusb_init(&ctx);
    LOGE("libusb_init: %d", re);
    if (re != LIBUSB_SUCCESS) {
        libusb_exit(ctx);
        return re;
    }
    re = libusb_wrap_sys_device(NULL, (intptr_t) fileDescriptor, &devh);
    LOGE("libusb_wrap_sys_device: %d", re);
    if (re != LIBUSB_SUCCESS) {
        libusb_exit(ctx);
        return re;
    }

    auto device = libusb_get_device(devh);
    libusb_device_descriptor desc{};
    re = libusb_get_device_descriptor(device, &desc);
    LOGE("libusb_get_device_descriptor: %d", re);
    if (re != LIBUSB_SUCCESS) {
        libusb_exit(ctx);
        return re;
    }
    LOGE("Dev (bus %u, device %u): %04X - %04X\n",
         libusb_get_bus_number(device), libusb_get_device_address(device),
         desc.idVendor, desc.idProduct);

    unsigned char string[256];

    if (!devh)
        libusb_open(device, &devh);

    if (devh) {
        if (desc.iManufacturer) {
            re = libusb_get_string_descriptor_ascii(devh, desc.iManufacturer, string,
                                                    sizeof(string));
            if (re > 0)
                LOGE("  Manufacturer:                   %s\n", (char *) string);
        }

        if (desc.iProduct) {
            re = libusb_get_string_descriptor_ascii(devh, desc.iProduct, string, sizeof(string));
            if (re > 0)
                LOGE("  Product:                   %s\n", (char *) string);
        }

//        if (desc.iSerialNumber && verbose) {
        if (desc.iSerialNumber) {
            re = libusb_get_string_descriptor_ascii(devh, desc.iSerialNumber, string,
                                                    sizeof(string));
            if (re > 0)
                LOGE("  SerialNumber:                   %s\n", (char *) string);
        }
    }
    //
    libusb_exit(ctx);
    return 0;
}
