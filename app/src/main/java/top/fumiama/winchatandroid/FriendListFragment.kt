package top.fumiama.winchatandroid

import android.app.AlertDialog
import android.content.ClipData
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.edit
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_list.*
import top.fumiama.winchatandroid.MainActivity.Companion.mainWeakReference
import top.fumiama.winchatandroid.client.Command
import top.fumiama.winchatandroid.client.GroupJoinQuit
import top.fumiama.winchatandroid.client.User
import top.fumiama.winchatandroid.databinding.FragmentListBinding
import top.fumiama.winchatandroid.ui.GroupListFragment
import top.fumiama.winchatandroid.ui.ListViewHolder
import java.io.File
import java.nio.ByteBuffer

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FriendListFragment : Fragment() {

    private var pref: SharedPreferences? = null

    private var _binding: FragmentListBinding? = null

    private var ad: FriendListViewHolderInstance.RecyclerViewAdapter? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ad = FriendListViewHolderInstance(ffr).RecyclerViewAdapter()
        pref = PreferenceManager.getDefaultSharedPreferences(requireContext())
        friendListFragmentHandler = FriendListFragmentHandler(this, Looper.myLooper()!!)
        ffr.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = ad
            setOnScrollChangeListener { _, _, scrollY, _, _ -> binding.ffsw.isEnabled = scrollY == 0  }
        }
        ad?.refresh()
        binding.ffsw.setOnRefreshListener {
            if(user == null) { // ?????????
                binding.root.postDelayed({
                    findNavController().navigate(R.id.action_FriendListFragment_to_LoginFragment)
                }, 233)
            } else {
                user?.udp?.let { udp ->
                    Thread {
                        val d = ByteArray(5)
                        d[0] = GroupListFragment.TYP_LST_ONLINE
                        ByteBuffer.wrap(d, 1, 4).putInt(0)
                        try {
                            udp.send(Command(Command.CMD_TYPE_GRP_LST, d).marshal())
                        } catch (e: Exception) {
                            e.printStackTrace()
                            mainWeakReference?.get()?.runOnUiThread {
                                Toast.makeText(context, "${e.cause}: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }.start()
                }
            }
            binding.ffsw.isRefreshing = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        ad = null
        friendListFragmentHandler = null
    }

    fun insertRow(b: Bundle) {
        val msg = b.getString("msg", "N/A")
        val id = b.getInt("id", 0)
        b.getString("name")?.let { nm ->
            if(nm.isNotEmpty()) pref?.edit {
                putString("name$id", nm)
                apply()
            }
        }
        Log.d("MyFLF", "id: $id, msg: $msg")
        pref?.getString(id.toString(), "")?.let { dataStr ->
            if(dataStr == "" || ad?.idDataMap?.containsKey(id) != true) { // new msg
                val data = arrayOf(pref?.getString("name$id", mainWeakReference?.get()?.getString(R.string.name_anonymous))!!, msg, "1")
                Log.d("MyFLF", "is new message, data: ${data[0]}\n" +
                        "${data[1]}\n" +
                        data[2]
                )
                pref?.edit {
                    putString(id.toString(), "${data[0]}\n${data[1]}\n${data[2]}")
                    apply()
                }
                try {
                    ad?.apply {
                        idDataMap[id] = data
                        add(id)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    mainWeakReference?.get()?.runOnUiThread {
                        Toast.makeText(context, "${e.cause}: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                val data = dataStr.split("\n").toTypedArray()
                Log.d("MyFLF", "is old message, data: $${data[0]}\n" +
                        "${data[1]}\n" +
                        data[2]
                )
                data[2] = (data[2].toInt()+1).toString()
                pref?.edit {
                    putString(id.toString(), "${data[0]}\n${data[1]}\n${data[2]}")
                    apply()
                }
                try {
                    ad?.apply {
                        idDataMap[id] = data
                        replace(id)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    mainWeakReference?.get()?.runOnUiThread {
                        Toast.makeText(context, "${e.cause}: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    inner class FriendListViewHolderInstance(itemView: View): ListViewHolder(itemView) {
        private val onLineClicked = { id: Int, data: Array<String>, _: View ->
            val bundle = Bundle()
            bundle.putInt("id", id)
            bundle.putString("name", data[0])
            bundle.putInt("count", data[2].toInt())
            findNavController().navigate(R.id.action_FriendListFragment_to_ChatFragment, bundle)
        }
        private val onLineLongClickedTypes = resources.getStringArray(R.array.line_friend_choice_types)
        private val onLineLongClicked = { id: Int, _: Array<String>, _: View ->
            AlertDialog.Builder(context)
                .setTitle(R.string.choice_title)
                .setIcon(R.mipmap.ic_launcher)
                .setSingleChoiceItems(ArrayAdapter(context!!, R.layout.line_choice, onLineLongClickedTypes), 0) { d, p ->
                    when(p) {
                        0 -> {
                            mainWeakReference?.get()?.apply {
                                cm?.apply {
                                    setPrimaryClip(ClipData.newPlainText(mainWeakReference?.get()?.getString(R.string.app_name)!!, "$id"))
                                    runOnUiThread {
                                        Toast.makeText(context, R.string.toast_ID_copied, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                        1 -> try {
                            pref?.edit {
                                remove(id.toString())
                                apply()
                                Log.d("MyFLF", "removed pref of id $id")
                            }
                            ad?.apply {
                                idDataMap.remove(id)
                                remove(id)
                                Log.d("MyFLF", "removed ad of id $id")
                            }
                            mainWeakReference?.get()?.msgFolder?.apply {
                                File(this, "$id").delete()
                            }
                            pref?.getString("name$id", mainWeakReference?.get()?.getString(R.string.name_anonymous)!!)?.let {
                                if(it.startsWith("grp ")) Thread {
                                    try {
                                        user?.udp?.apply {
                                            send(Command(Command.CMD_TYPE_GRP_QUIT, GroupJoinQuit(id).marshal()).marshal())
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        mainWeakReference?.get()?.runOnUiThread {
                                            Toast.makeText(context, "${e.cause}: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }.start()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            mainWeakReference?.get()?.runOnUiThread {
                                Toast.makeText(context, "${e.cause}: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    d.cancel()
                }
                .show()
            true
        }
        inner class RecyclerViewAdapter: ListViewHolder.RecyclerViewAdapter(onLineClicked, onLineLongClicked) {
            val idDataMap = hashMapOf<Int, Array<String>>()
            override fun getKeys(): List<Int> {
                pref?.all?.forEach { e ->
                    e.key.toIntOrNull()?.let { id ->
                        Log.d("MyFLF", "load friend: $id")
                        val data = e.value.toString().split("\n").toTypedArray()
                        if(data.size != 3) return@forEach
                        idDataMap[id] = data
                    }
                }
                return idDataMap.keys.distinct()
            }

            override fun getValue(id: Int): Array<String>? {
                return idDataMap[id]
            }
        }
    }

    companion object {
        var user: User? = null
        var friendListFragmentHandler: FriendListFragmentHandler? = null
    }
}
