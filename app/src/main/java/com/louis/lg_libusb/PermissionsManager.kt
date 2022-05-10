package com.louis.lg_libusb

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

/**
 * Created by louisgeek on 2022/5/11.
 */
object PermissionsManager {
    private const val TAG = "PermissionsManager"
    private lateinit var permissionsARLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var appContext: Context
    private var cb: (() -> Unit)? = null
    private val permissions = arrayOf(Manifest.permission.CAMERA)

    private val defaultLifecycleObserver = object : DefaultLifecycleObserver {
        override fun onCreate(owner: LifecycleOwner) {
//                super.onCreate(owner)
            //
            val fragmentActivity = owner as FragmentActivity
            val context = owner as Context
            //
            Log.e(TAG, "onCreate: ")
            //
            checkPermission()
        }

        override fun onDestroy(owner: LifecycleOwner) {
//            super.onDestroy(owner)
        }
    }

    fun init(fragmentActivity: FragmentActivity, callback: (() -> Unit)?) {
        val context = fragmentActivity as Context
        appContext = context.applicationContext
        cb = callback
        //
        permissionsARLauncher =
            fragmentActivity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grantedMap ->
                if (grantedMap.values.all { granted -> granted == true }) {
                    permissionGranted()
                } else {
                    var canShow = false
                    grantedMap.keys.forEach { permission ->
                        val shouldShowRationale =
                            ActivityCompat.shouldShowRequestPermissionRationale(
                                fragmentActivity,
                                permission
                            )
                        if (shouldShowRationale) {
                            canShow = true
                        }
                    }
                    if (canShow) {
                        //
                        checkPermission()
                    } else {
                        Toast.makeText(
                            appContext,
                            "shouldShowRationale = false",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        //
        fragmentActivity.lifecycle.addObserver(defaultLifecycleObserver)
    }

    private fun checkPermission() {
        Log.e(TAG, "checkPermission: ")
        if (isPermissionGranted()) {
            permissionGranted()
        } else {
            permissionsARLauncher.launch(permissions)
        }
    }

    private fun isPermissionGranted(): Boolean {
        return permissions.all { permission ->
            ContextCompat.checkSelfPermission(
                appContext,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun permissionGranted() {
        cb?.invoke()
    }
}