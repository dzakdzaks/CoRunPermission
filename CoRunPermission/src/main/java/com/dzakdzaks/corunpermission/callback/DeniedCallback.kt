package com.dzakdzaks.corunpermission.callback

import com.dzakdzaks.corunpermission.PermissionResult

interface DeniedCallback {
    fun onDenied(result: PermissionResult)
}