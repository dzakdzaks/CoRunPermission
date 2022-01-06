package com.dzakdzaks.corunpermission

import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import java.util.*

/**
 * DO NOT USE THIS FRAGMENT DIRECTLY!
 * It's only here because fragments have to be public
 */
class PermissionFragment : Fragment() {
    private val permissionsList: MutableList<String> = ArrayList()
    private var listener: PermissionListener? = null
    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            onPermissionResult(permissions.entries)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val arguments = arguments
        if (arguments != null) {
            val permissionsArgs: List<String>? = arguments.getStringArrayList(LIST_PERMISSIONS)
            if (permissionsArgs != null) {
                permissionsList.addAll(permissionsArgs)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (permissionsList.size > 0) {
            requestPermission.launch(permissionsList.toTypedArray())
        } else {
            // this shouldn't happen, but just to be sure
            parentFragmentManager.beginTransaction()
                .remove(this)
                .commitAllowingStateLoss()
        }
    }

    private fun onPermissionResult(resultPermission: MutableSet<MutableMap.MutableEntry<String, Boolean>>) {
        val listener = listener
        val acceptedPermissions: MutableList<String> = ArrayList()
        val askAgainPermissions: MutableList<String> = ArrayList()
        val refusedPermissions: MutableList<String> = ArrayList()
        resultPermission.forEach {
            if (it.value) {
                acceptedPermissions.add(it.key)
            } else {
                if (shouldShowRequestPermissionRationale(it.key)) {
                    //listener.onDenied(permissionResult);
                    askAgainPermissions.add(it.key)
                } else {
                    refusedPermissions.add(it.key)
                    //listener.onForeverDenied(permissionResult);
                }
            }
        }
        listener?.onRequestPermissionsResult(
            acceptedPermissions,
            refusedPermissions,
            askAgainPermissions
        )
        parentFragmentManager.beginTransaction()
            .remove(this)
            .commitAllowingStateLoss()
    }

    fun setListener(listener: PermissionListener?): PermissionFragment {
        this.listener = listener
        return this
    }

    interface PermissionListener {
        fun onRequestPermissionsResult(
            acceptedPermissions: List<String>?,
            refusedPermissions: List<String>?,
            askAgainPermissions: List<String>?
        )
    }

    companion object {
        const val LIST_PERMISSIONS = "LIST_PERMISSIONS"
        fun newInstance(permissions: List<String>): PermissionFragment {
            val args = Bundle()
            args.putStringArrayList(LIST_PERMISSIONS, ArrayList(permissions))
            val fragment = PermissionFragment()
            fragment.arguments = args
            return fragment
        }
    }
}