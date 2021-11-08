package com.dzakdzaks.corunpermission.callback

import com.dzakdzaks.corunpermission.PermissionResult

interface PermissionListener {
    fun onAccepted(permissionResult: PermissionResult, accepted: List<String?>)
    fun onDenied(
        permissionResult: PermissionResult,
        denied: List<String?>,
        foreverDenied: List<String?>
    )
}