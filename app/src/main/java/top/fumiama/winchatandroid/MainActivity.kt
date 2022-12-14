package top.fumiama.winchatandroid

import android.content.ClipboardManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Message
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.navigation.*
import kotlinx.android.synthetic.main.activity_main.*
import top.fumiama.winchatandroid.databinding.ActivityMainBinding
import java.io.File
import java.lang.ref.WeakReference

class MainActivity : AppCompatActivity() {

    var cm: ClipboardManager? = null
    var msgFolder: File? = null

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        mainWeakReference = WeakReference(this)
        cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        msgFolder = getExternalFilesDir("msg")

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when(destination.id) {
                R.id.FriendListFragment -> fab.visibility = View.VISIBLE
                else -> fab.visibility = View.GONE
            }
        }

        var i = 123
        binding.fab.setOnClickListener { view ->
            val msg = Message.obtain(FriendListFragment.friendListFragmentHandler, FriendListFragmentHandler.FRIEND_LST_F_MSG_INSERT_ROW)
            val data = Bundle()
            data.putInt("id", i)
            data.putString("msg", "test $i")
            i++
            msg.data = data
            msg.sendToTarget()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> {
                val navController = findNavController(R.id.nav_host_fragment_content_main)
                when(navController.currentDestination?.id) {
                    R.id.FriendListFragment -> navController.navigate(R.id.action_FriendListFragment_to_SettingsFragment)
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

    companion object{
        var mainWeakReference: WeakReference<MainActivity>? = null
    }
}
