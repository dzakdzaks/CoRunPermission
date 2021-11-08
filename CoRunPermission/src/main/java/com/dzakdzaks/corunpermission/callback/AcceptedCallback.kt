package com.dzakdzaks.corunpermission.callback

import com.dzakdzaks.corunpermission.PermissionResult

interface AcceptedCallback {
    fun onAccepted(result: PermissionResult)
}