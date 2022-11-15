package top.fumiama.winchatandroid

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import top.fumiama.winchatandroid.net.UDP
import java.net.InetAddress
import java.net.URI

class SettingsFragment: PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.pref_setting, rootKey)
        //if(settingsPref == null) settingsPref = context?.let { PreferenceManager.getDefaultSharedPreferences(it) }
        findPreference<EditTextPreference>("settings_server_ep")?.setOnPreferenceChangeListener { _, newValue ->
            URI(newValue.toString()).parseServerAuthority()?.let {
                ep = UDP(it.host, it.port)

            }
            return@setOnPreferenceChangeListener true
        }
    }

    companion object {
        //var settingsPref: SharedPreferences? = SettingsFragment().context?.let {PreferenceManager.getDefaultSharedPreferences(it)}
        var ep: UDP? = null
    }
}
