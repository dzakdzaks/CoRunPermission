package com.dzakdzaks.corunpermission


class PermissionException(val permissionResult: PermissionResult) : Exception() {

    val accepted: List<String> = permissionResult.getAccepted()
    val foreverDenied: List<String> = permissionResult.getForeverDenied()
    val denied: List<String> = permissionResult.getDenied()
    val runtimePermission: RuntimePermission = permissionResult.runtimePermission

    fun goToSettings() {
        permissionResult.goToSettings()
    }

    fun askAgain() {
        permissionResult.askAgain()
    }

    fun isAccepted(): Boolean {
        return permissionResult.isAccepted()
    }

    fun hasDenied(): Boolean {
        return permissionResult.hasDenied()
    }

    fun hasForeverDenied(): Boolean {
        return permissionResult.hasForeverDenied()
    }

}