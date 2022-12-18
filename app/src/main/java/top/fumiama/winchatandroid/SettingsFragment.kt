package top.fumiama.winchatandroid

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import top.fumiama.winchatandroid.net.TCP
import top.fumiama.winchatandroid.net.UDP
import java.net.URI

class SettingsFragment: PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.pref_setting, rootKey)
        findPreference<EditTextPreference>("settings_server_ep")?.apply {
            PreferenceManager.getDefaultSharedPreferences(context)?.getString("settings_server_ep", "")?.let { eps ->
                if(eps != "") {
                    summary = eps
                }
            }
            setOnPreferenceChangeListener { _, newValue ->
                URI("my://$newValue").parseServerAuthority()?.let {
                    Log.d("MySF", "host: ${it.host}, port: ${it.port}")
                    ep = UDP(it.host, it.port)
                }
                return@setOnPreferenceChangeListener true
            }
        }
    }

    companion object {
        private var ep: UDP? = null
        fun getUDP(context: Context): UDP {
            if(ep != null) return ep!!
            ep = PreferenceManager.getDefaultSharedPreferences(context).getString("settings_server_ep", null)?.let { s ->
                    URI("my://$s").parseServerAuthority()?.let {
                        Log.d("MySF", "init: set host: ${it.host}, port: ${it.port}")
                        UDP(it.host, it.port)
                    }
                }
            return ep!!
        }
        fun getTCP(context: Context, port: Short) = PreferenceManager.getDefaultSharedPreferences(context).getString("settings_server_ep", null)?.let { s ->
            URI("my://$s").parseServerAuthority()?.let {
                Log.d("MySF", "tcp: get host: ${it.host}, port: ${it.port}")
                TCP(it.host, port.toInt())
            }
        }
    }
}
