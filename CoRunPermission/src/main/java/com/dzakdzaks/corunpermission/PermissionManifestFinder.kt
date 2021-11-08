package com.dzakdzaks.corunpermission

import android.annotation.TargetApi
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import java.util.ArrayList

object PermissionManifestFinder {
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    fun findNeededPermissionsFromManifest(context: Context): List<String> {
        val pm = context.packageManager
        var info: PackageInfo? = null
        try {
            info = pm.getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS)
        } catch (e: PackageManager.NameNotFoundException) { /* */
        }
        val needed: MutableList<String> = ArrayList()
        if (info?.requestedPermissions != null && info.requestedPermissionsFlags != null) {
            for (i in info.requestedPermissions.indices) {
                val flags = info.requestedPermissionsFlags[i]
                var group: String? = null
                try {
                    group = pm.getPermissionInfo(info.requestedPermissions[i], 0).group
                } catch (e: PackageManager.NameNotFoundException) { /* */
                }
                if (flags and PackageInfo.REQUESTED_PERMISSION_GRANTED == 0 && group != null) {
                    needed.add(info.requestedPermissions[i])
                }
            }
        }
        return needed
    }
}