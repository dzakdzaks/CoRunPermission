package com.dzakdzaks.corunpermission

import java.util.ArrayList

class PermissionResult(
    val runtimePermission: RuntimePermission,
    accepted: List<String>?,
    foreverDenied: List<String>?,
    denied: List<String>?
) {
    private val accepted: MutableList<String> = ArrayList()
    private val foreverDenied: MutableList<String> = ArrayList()
    private val denied: MutableList<String> = ArrayList()
    fun askAgain() {
        runtimePermission.ask()
    }

    fun isAccepted(): Boolean {
        return foreverDenied.isEmpty() && denied.isEmpty()
    }

    fun goToSettings() {
        runtimePermission.goToSettings()
    }

    fun hasDenied(): Boolean {
        return denied.isNotEmpty()
    }

    fun hasForeverDenied(): Boolean {
        return foreverDenied.isNotEmpty()
    }

    fun getAccepted(): List<String> {
        return accepted
    }

    fun getForeverDenied(): List<String> {
        return foreverDenied
    }

    fun getDenied(): List<String> {
        return denied
    }

    init {
        if (accepted != null) {
            this.accepted.addAll(accepted)
        }
        if (foreverDenied != null) {
            this.foreverDenied.addAll(foreverDenied)
        }
        if (denied != null) {
            this.denied.addAll(denied)
        }
    }
}