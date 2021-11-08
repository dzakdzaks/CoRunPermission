package com.dzakdzaks.corunpermission

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.dzakdzaks.corunpermission.callback.*
import java.lang.ref.Reference
import java.lang.ref.WeakReference
import java.util.*

class RuntimePermission(activity: FragmentActivity?) {
    private var activityReference: Reference<FragmentActivity?>? = null

    //The list of permissions we want to ask
    private val permissionsToRequest: MutableList<String> = ArrayList()

    //region callbacks
    private val responseCallbacks: MutableList<ResponseCallback> = ArrayList<ResponseCallback>()
    private val acceptedCallbacks: MutableList<AcceptedCallback> = ArrayList<AcceptedCallback>()
    private val foreverDeniedCallbacks: MutableList<ForeverDeniedCallback> =
        ArrayList<ForeverDeniedCallback>()
    private val deniedCallbacks: MutableList<DeniedCallback> = ArrayList<DeniedCallback>()
    private val permissionListeners: MutableList<PermissionListener> =
        ArrayList<PermissionListener>()

    //the listener we will give to the fragment
    private val listener: PermissionFragment.PermissionListener =
        object : PermissionFragment.PermissionListener {
            override fun onRequestPermissionsResult(
                acceptedPermissions: List<String>?,
                refusedPermissions: List<String>?,
                askAgainPermissions: List<String>?
            ) {
                onReceivedPermissionResult(
                    acceptedPermissions,
                    refusedPermissions,
                    askAgainPermissions
                )
            }
        }

    /**
     * Just a helper methods in case the user blocks permission.
     * It goes to your application settings page for the user to enable permission again.
     */
    fun goToSettings() {
        val fragmentActivity = activityReference!!.get()
        fragmentActivity?.startActivity(
            Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", fragmentActivity.packageName, null)
            )
        )
    }

    private fun onReceivedPermissionResult(
        acceptedPermissions: List<String>?,
        refusedPermissions: List<String>?,
        askAgainPermissions: List<String>?
    ) {
        val permissionResult =
            PermissionResult(this, acceptedPermissions, refusedPermissions, askAgainPermissions)
        if (permissionResult.isAccepted()) {
            for (callback in acceptedCallbacks) {
                callback.onAccepted(permissionResult)
            }
            for (permissionListener in permissionListeners) {
                permissionListener.onAccepted(permissionResult, permissionResult.getAccepted())
            }
        }
        if (permissionResult.hasDenied()) {
            for (callback in deniedCallbacks) {
                callback.onDenied(permissionResult)
            }
        }
        if (permissionResult.hasForeverDenied()) {
            for (callback in foreverDeniedCallbacks) {
                callback.onForeverDenied(permissionResult)
            }
        }
        if (permissionResult.hasForeverDenied() || permissionResult.hasDenied()) {
            for (permissionListener in permissionListeners) {
                permissionListener.onDenied(
                    permissionResult,
                    permissionResult.getDenied(),
                    permissionResult.getForeverDenied()
                )
            }
        }
        for (responseCallback in responseCallbacks) {
            responseCallback.onResponse(permissionResult)
        }
    }

    /**
     * We want to only request given permissions
     * If we do not call this method, the library will find all needed permissions to ask from manifest
     *
     * @see android.Manifest.permission
     */
    fun request(permissions: List<String>?): RuntimePermission {
        if (permissions != null) {
            permissionsToRequest.clear()
            permissionsToRequest.addAll(permissions)
        }
        return this
    }

    /**
     * We want to only request given permissions
     *
     * @see android.Manifest.permission
     */
    fun request(vararg permissions: String): RuntimePermission {
        return this.request(permissions.toList())
    }

    fun onResponse(callback: ResponseCallback?): RuntimePermission {
        if (callback != null) {
            responseCallbacks.add(callback)
        }
        return this
    }

    fun onResponse(permissionListener: PermissionListener?): RuntimePermission {
        if (permissionListener != null) {
            permissionListeners.add(permissionListener)
        }
        return this
    }

    fun onAccepted(callback: AcceptedCallback?): RuntimePermission {
        if (callback != null) {
            acceptedCallbacks.add(callback)
        }
        return this
    }

    fun onDenied(callback: DeniedCallback?): RuntimePermission {
        if (callback != null) {
            deniedCallbacks.add(callback)
        }
        return this
    }

    fun onForeverDenied(callback: ForeverDeniedCallback?): RuntimePermission {
        if (callback != null) {
            foreverDeniedCallbacks.add(callback)
        }
        return this
    }

    fun ask(responseCallback: ResponseCallback?) {
        onResponse(responseCallback)
            .ask()
    }

    fun ask(permissionListener: PermissionListener?) {
        onResponse(permissionListener)
            .ask()
    }

    /**
     * If we request permission using .request(names), we only ask them
     * If not, this lib will search needed permissions from Manifest
     */
    private fun findNeededPermissions(context: Context): List<String> {
        return if (permissionsToRequest.isEmpty()) {
            PermissionManifestFinder.findNeededPermissionsFromManifest(context)
        } else {
            permissionsToRequest
        }
    }

    /**
     * Ask for the permission. Which permission? Anything you register on your manifest that needs it.
     * It is safe to call this every time without querying `shouldAsk`.
     * In case you call `ask` without any permission, the method returns.
     */
    fun ask() {
        val activity = activityReference!!.get()
        if (activity == null || activity.isFinishing) {
            return
        }

        //retrieve permissions we want
        val permissions = findNeededPermissions(activity)

        // No need to ask for permissions on API levels below Android Marshmallow
        if (permissions.isEmpty() || Build.VERSION.SDK_INT < Build.VERSION_CODES.M || arePermissionsAlreadyAccepted(
                activity,
                permissions
            )
        ) {
            onAllAccepted(permissions)
        } else {
            val oldFragment: PermissionFragment? = activity
                .supportFragmentManager
                .findFragmentByTag(TAG) as PermissionFragment?
            if (oldFragment != null) {
                oldFragment.setListener(listener)
            } else {
                val newFragment: PermissionFragment = PermissionFragment.newInstance(permissions)
                newFragment.setListener(listener)
                activity.runOnUiThread(Runnable {
                    activity.supportFragmentManager
                        .beginTransaction()
                        .add(newFragment, TAG)
                        .commitAllowingStateLoss()
                    // change to .commitNowAllowingStateLoss() to see the crash
                })
            }
        }
    }

    private fun arePermissionsAlreadyAccepted(
        context: Context,
        permissions: List<String>
    ): Boolean {
        for (permission in permissions) {
            val permissionState = ContextCompat.checkSelfPermission(context, permission)
            if (permissionState == PackageManager.PERMISSION_DENIED) {
                return false
            }
        }
        return true
    }

    private fun onAllAccepted(permissions: List<String>) {
        onReceivedPermissionResult(permissions, null, null)
    }

    fun askAgain() {}

    companion object {
        private const val TAG = "PERMISSION_FRAGMENT"

        /**
         * Fill permissions to only ask If we do not call this method,
         * If not set or empty, the library will find all needed permissions to ask from manifest
         * You can call .request(permissions) after this method if you want to give permissions in a separate method
         */
        fun askPermission(
            activity: FragmentActivity?,
            vararg permissions: String
        ): RuntimePermission {
            return RuntimePermission(activity).request(permissions.toList())
        }

        /**
         * Fill permissions to only ask If we do not call this method,
         * If not set or empty, the library will find all needed permissions to ask from manifest
         * You can call .request(permissions) after this method if you want to give permissions in a separate method
         */
        fun askPermission(fragment: Fragment?, vararg permissions: String): RuntimePermission {
            var activity: FragmentActivity? = null
            if (fragment != null) {
                activity = fragment.activity
            }
            return askPermission(activity).request(permissions.toList())
        }
    }

    //endregion
    init {
        if (activity != null) {
            activityReference = WeakReference(activity)
        } else {
            activityReference = WeakReference(null)
        }
    }
}