package com.dzakdzaks.coroutineruntimepermission

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dzakdzaks.corunpermission.PermissionException
import com.dzakdzaks.corunpermission.askPermission
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<TextView>(R.id.tv).setOnClickListener {
            lifecycleScope.launch {
                try {
                    askPermission(Manifest.permission.READ_CONTACTS)
                    Log.d("walwaw", "permission accepted")
                } catch (e: PermissionException) {
                    if (e.hasDenied()) {
                        Log.d("walwaw", "permission hasDenied")
                        AlertDialog.Builder(this@MainActivity)
                            .setMessage("Please accept our permissions")
                            .setPositiveButton("yes") { dialog, which ->
                                e.askAgain()
                            }
                            .setNegativeButton("no") { dialog, which ->
                                dialog.dismiss()
                            }
                            .show()
                    }
                    if (e.hasForeverDenied()) {
                        Log.d("walwaw", "permission hasForeverDenied")
                        AlertDialog.Builder(this@MainActivity)
                            .setMessage("Please accept our permissions on settings")
                            .setPositiveButton("yes") { dialog, which ->
                                e.goToSettings()
                            }
                            .setNegativeButton("no") { dialog, which ->
                                dialog.dismiss()
                            }
                            .show()

                    }
                }
            }
        }
    }
}