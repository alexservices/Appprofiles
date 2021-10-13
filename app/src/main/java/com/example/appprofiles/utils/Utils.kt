package com.example.appprofiles.utils

import android.app.Activity
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class Utils {
    companion object {
        fun checkPermissions(permission:String,requestCode:Int,activity: Activity)
        {
            if (ContextCompat.checkSelfPermission(
                    activity,
                    permission
                ) == PackageManager.PERMISSION_DENIED
            ) {
                ActivityCompat.requestPermissions(activity, arrayOf(permission), requestCode)
            } else {
                Toast.makeText(activity, "Permisos concedidos", Toast.LENGTH_SHORT)
                    .show()
            }

        }
    }

}