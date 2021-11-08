package com.dzakdzaks.corunpermission.callback

import com.dzakdzaks.corunpermission.PermissionResult

interface ResponseCallback {
    fun onResponse(result: PermissionResult)
}