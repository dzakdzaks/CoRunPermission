package com.dzakdzaks.corunpermission.callback

import com.dzakdzaks.corunpermission.PermissionResult

interface ForeverDeniedCallback {
    fun onForeverDenied(result: PermissionResult)
}