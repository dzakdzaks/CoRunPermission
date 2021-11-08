package com.dzakdzaks.corunpermission

import com.dzakdzaks.corunpermission.callback.ResponseCallback
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

suspend fun androidx.fragment.app.FragmentActivity.askPermission(vararg permissions: String): PermissionResult =
    suspendCoroutine { continuation ->
        var resumed = false
        RuntimePermission.askPermission(this)
            .request(permissions.toList())
            .onResponse(object : ResponseCallback{
                override fun onResponse(result: PermissionResult) {
                    if (!resumed) {
                        resumed = true
                        when {
                            result.isAccepted() -> continuation.resume(result)
                            else -> continuation.resumeWithException(PermissionException(result))
                        }
                    }
                }

            })
            .ask()
    }

suspend fun androidx.fragment.app.Fragment.askPermission(vararg permissions: String): PermissionResult =
    suspendCoroutine { continuation ->
        var resumed = false
        when (activity) {
            null -> continuation.resumeWithException(NoActivityException())
            else -> RuntimePermission.askPermission(this)
                .request(permissions.toList())
                .onResponse(object : ResponseCallback {
                    override fun onResponse(result: PermissionResult) {
                        if (!resumed) {
                            resumed = true
                            when {
                                result.isAccepted() -> continuation.resume(result)
                                else -> continuation.resumeWithException(PermissionException(result))
                            }
                        }
                    }

                })
                .ask()
        }
    }
