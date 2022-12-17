package top.fumiama.winchatandroid.ui

import android.content.ClipData
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_list.*
import top.fumiama.winchatandroid.FriendListFragment.Companion.user
import top.fumiama.winchatandroid.MainActivity
import top.fumiama.winchatandroid.R
import top.fumiama.winchatandroid.client.Command
import top.fumiama.winchatandroid.databinding.FragmentListBinding
import java.lang.ref.WeakReference
import java.nio.ByteBuffer

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
open class GroupListFragment(private val typ: Byte) : Fragment() {

    private var _binding: FragmentListBinding? = null

    private var ad: ListViewHolderInstance.RecyclerViewAdapter? = null

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

        ad = ListViewHolderInstance(ffr).RecyclerViewAdapter()
        groupListFragmentAdapterWeakReference = WeakReference(ad)
        ffr.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = ad
            setOnScrollChangeListener { _, _, scrollY, _, _ -> binding.ffsw.isEnabled = scrollY == 0  }
        }
        ad?.refresh()
        binding.ffsw.setOnRefreshListener {
            ad?.refresh()
            binding.ffsw.isRefreshing = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        ad = null
        groupListFragmentAdapterWeakReference = null
    }

    inner class ListViewHolderInstance(itemView: View): ListViewHolder(itemView) {
        private val onLineClicked = { _: Int, data: Array<String>, _: View ->
            MainActivity.mainWeakReference?.get()?.apply {
                cm?.apply {
                    setPrimaryClip(ClipData.newPlainText(MainActivity.mainWeakReference?.get()?.getString(
                            R.string.app_name)?:"WinChatAndroid", data[1]))
                    runOnUiThread {
                        Toast.makeText(context, R.string.toast_ID_copied, Toast.LENGTH_SHORT).show()
                    }
                }
            }
            Unit
        }
        inner class RecyclerViewAdapter: ListViewHolder.RecyclerViewAdapter(onLineClicked) {
            val idDataMap = hashMapOf<Int, Array<String>>()
            override fun getKeys(): List<Int> {
                arguments?.getInt("id")?.let { grpID ->
                    user?.udp?.let { udp ->
                        Thread {
                            val d = ByteArray(5)
                            d[0] = typ
                            ByteBuffer.wrap(d, 1, 4).putInt(grpID)
                            try {
                                udp.send(Command(Command.CMD_TYPE_GRP_LST, d).marshal())
                            } catch (e: Exception) {
                                e.printStackTrace()
                                MainActivity.mainWeakReference?.get()?.runOnUiThread {
                                    Toast.makeText(context, "${e.cause}: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }.start()
                    }
                }
                Toast.makeText(context, R.string.toast_get_data, Toast.LENGTH_SHORT).show()
                return idDataMap.keys.distinct()
            }

            override fun getValue(id: Int): Array<String>? {
                return idDataMap[id]
            }
        }
    }

    companion object {
        const val TYP_LST_MEMBER: Byte = 0
        const val TYP_LST_FILE: Byte = 1
        var groupListFragmentAdapterWeakReference: WeakReference<ListViewHolderInstance.RecyclerViewAdapter>? = null
    }
}
