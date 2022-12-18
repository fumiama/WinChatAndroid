package top.fumiama.winchatandroid

import android.content.ClipboardManager
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.provider.OpenableColumns
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.WindowCompat
import androidx.navigation.*
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.preference.PreferenceManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_input.view.*
import top.fumiama.winchatandroid.FriendListFragment.Companion.user
import top.fumiama.winchatandroid.client.Command
import top.fumiama.winchatandroid.client.GroupJoinQuit
import top.fumiama.winchatandroid.databinding.ActivityMainBinding
import java.io.File
import java.io.FileInputStream
import java.lang.ref.WeakReference


class MainActivity : AppCompatActivity() {

    var cm: ClipboardManager? = null
    var msgFolder: File? = null

    var menuMain: Menu? = null

    val fileSelector = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { it ->
        it.data?.data?.let { uri ->
            Thread {
                val inputFile = File(cacheDir, "input")
                val fd = contentResolver.openFileDescriptor(uri, "r")
                fd?.fileDescriptor?.let { it ->
                    val fi = FileInputStream(it)
                    inputFile.outputStream().let { os ->
                        fi.copyTo(os)
                        os.close()
                    }
                    fi.close()
                }
                fd?.close()
                ChatFragment.sendFileCallBack(inputFile, getNameFromURI(uri))
            }.start()
        }
    }

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        mainWeakReference = WeakReference(this)
        cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        msgFolder = getExternalFilesDir("msg")
        msgFolder?.setReadable(true)
        msgFolder?.setWritable(true)
        msgFolder?.setExecutable(true)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.FriendListFragment -> {
                    fab.visibility = View.VISIBLE
                    menuMain?.apply {
                        findItem(R.id.action_settings)?.isVisible = true
                        findItem(R.id.action_group_members)?.isVisible = false
                        findItem(R.id.action_group_file)?.isVisible = false
                    }
                }
                R.id.ChatFragment -> {
                    fab.visibility = View.GONE
                    menuMain?.apply {
                        findItem(R.id.action_settings)?.isVisible = false
                        findItem(R.id.action_group_members)?.isVisible = true
                        findItem(R.id.action_group_file)?.isVisible = true
                    }
                }
                else -> {
                    fab.visibility = View.GONE
                    menuMain?.apply {
                        findItem(R.id.action_settings)?.isVisible = false
                        findItem(R.id.action_group_members)?.isVisible = false
                        findItem(R.id.action_group_file)?.isVisible = false
                    }
                }
            }
        }

        binding.fab.setOnClickListener {
            val t = layoutInflater.inflate(R.layout.dialog_input, null, false)
            AlertDialog.Builder(this@MainActivity)
                .setView(t)
                .setTitle(R.string.dialog_add_user)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    val info = t.diet.text.toString().split(' ')
                    if(info.size != 2) {
                        Toast.makeText(this, R.string.toast_invalid_input, Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    info[0].toIntOrNull()?.let { fromID ->
                        val msg = Message.obtain(FriendListFragment.friendListFragmentHandler, FriendListFragmentHandler.FRIEND_LST_F_MSG_INSERT_ROW)
                        val data = Bundle()
                        data.putInt("id", fromID)
                        data.putString("msg", "create dialog")
                        msg.data = data
                        PreferenceManager.getDefaultSharedPreferences(this).edit {
                            putString("name$fromID", info[1])
                            apply()
                        }
                        msg.sendToTarget()
                    }?:Toast.makeText(this, R.string.toast_invalid_input, Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton(android.R.string.cancel) { _, _ -> }
                .show()
        }

        binding.fab.setOnLongClickListener {
            val t = layoutInflater.inflate(R.layout.dialog_input, null, false)
            AlertDialog.Builder(this@MainActivity)
                .setView(t)
                .setTitle(R.string.dialog_join_group)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    val info = t.diet.text.toString().split(' ')
                    if(info.size != 2) {
                        Toast.makeText(this, R.string.toast_invalid_input, Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    info[0].toIntOrNull()?.let { grpID ->
                        val msg = Message.obtain(FriendListFragment.friendListFragmentHandler, FriendListFragmentHandler.FRIEND_LST_F_MSG_INSERT_ROW)
                        val data = Bundle()
                        data.putInt("id", grpID)
                        data.putString("msg", "create dialog")
                        msg.data = data
                        PreferenceManager.getDefaultSharedPreferences(this).edit {
                            putString("name$grpID", "grp ${info[1]}")
                            apply()
                        }
                        msg.sendToTarget()
                        Thread {
                            try {
                                user?.udp?.send(Command(Command.CMD_TYPE_GRP_JOIN, GroupJoinQuit(grpID).marshal()).marshal())
                            } catch (e: Exception) {
                                e.printStackTrace()
                                mainWeakReference?.get()?.runOnUiThread {
                                    Toast.makeText(this, "${e.cause}: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }.start()
                    }?:Toast.makeText(this, R.string.toast_invalid_input, Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton(android.R.string.cancel) { _, _ -> }
                .show()
            return@setOnLongClickListener true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        menuMain = menu
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> {
                when(navController.currentDestination?.id) {
                    R.id.FriendListFragment -> navController.navigate(R.id.action_FriendListFragment_to_SettingsFragment)
                }
                true
            }
            R.id.action_group_file -> {
                when(navController.currentDestination?.id) {
                    R.id.ChatFragment -> ChatFragment.chatFragmentHandler?.sendEmptyMessage(ChatFragmentHandler.CHAT_F_NAV_TO_FILE_F)
                }
                true
            }
            R.id.action_group_members -> {
                when(navController.currentDestination?.id) {
                    R.id.ChatFragment -> ChatFragment.chatFragmentHandler?.sendEmptyMessage(ChatFragmentHandler.CHAT_F_NAV_TO_MEMBER_F)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    fun showNotice(msg: String) {
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_notice)
            .setMessage(msg)
            .setIcon(R.mipmap.ic_launcher)
            .setPositiveButton(android.R.string.ok) { _, _ -> }
            .show()
    }

    private fun getNameFromURI(uri: Uri?): String? {
        val c: Cursor? = uri?.let { contentResolver.query(it, null, null, null, null) }
        c?.moveToFirst()
        val i = c?.getColumnIndex(OpenableColumns.DISPLAY_NAME)?:0
        if (i < 0) {
            return null
        }
        return c?.getString(i)
    }

    companion object{
        var mainWeakReference: WeakReference<MainActivity>? = null
    }
}
