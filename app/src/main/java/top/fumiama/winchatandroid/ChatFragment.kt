package top.fumiama.winchatandroid

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.from_message.view.*
import kotlinx.android.synthetic.main.to_message.view.*
import top.fumiama.winchatandroid.LoginFragment.Companion.udp
import top.fumiama.winchatandroid.MainActivity.Companion.mainWeakReference
import top.fumiama.winchatandroid.client.Command
import top.fumiama.winchatandroid.client.Command.Companion.CMD_TYPE_MSG_TXT
import top.fumiama.winchatandroid.client.TextMessage
import top.fumiama.winchatandroid.databinding.FragmentChatBinding
import java.io.File

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        arguments?.apply {
            val fromID = getInt("id", 0)
            val count = getInt("count", 0)
            getString("name", "匿名")?.let { name ->
                mainWeakReference?.get()?.apply {
                    runOnUiThread { toolbar?.title = name }
                }
                Log.d("MyCF", "id: $fromID, count: $count, name: $name")
            }
            mainWeakReference?.get()?.msgFolder?.apply {
                File(this, "$fromID").apply {
                    if(!exists()) return binding.root
                    var data = readBytes()
                    while (data.isNotEmpty()) {
                        val cmd = Command(data)
                        when(cmd.typ) {
                            CMD_TYPE_MSG_TXT -> {
                                val txtMsg = TextMessage(cmd.data)
                                Log.d("MyCF", "load txt msg from ${txtMsg.fromID}, to ${txtMsg.toID}, msg: ${txtMsg.msg}")
                                val line = inflater.inflate(if(txtMsg.fromID == fromID) R.layout.from_message else R.layout.to_message, binding.cfl, false)
                                if(txtMsg.fromID == fromID) {
                                    line.frl.fromUsernameGroup.fromUsername.text = name
                                    line.frl.fromMessage.text = txtMsg.msg
                                    line.frl.fromUsernameGroup.icon_fb1.setBackgroundResource(R.drawable.ic_girlz_pic)
                                } else {
                                    line.tol.toUsernameGroup.toUsername.setText(R.string.name_me)
                                    line.tol.toMessage.text = txtMsg.msg
                                    line.tol.toUsernameGroup.icon_fb2.setBackgroundResource(R.drawable.ic_girl_pic)
                                }
                                binding.cfl.addView(line)
                                data = data.copyOfRange(cmd.length, data.size)
                            }
                        }
                    }
                }
            }
            binding.fcbsnd.setOnClickListener {
                if(binding.fctmsg.text.isEmpty()) return@setOnClickListener
                try {
                    FriendListFragment.user?.apply {
                        val d = Command(CMD_TYPE_MSG_TXT, TextMessage(userID(), fromID, binding.fctmsg.text.toString()).marshal()).marshal()
                        udp?.send(d)
                        mainWeakReference?.get()?.msgFolder?.apply {
                            File(this, "$fromID").apply {
                                if(!exists()) createNewFile()
                                appendBytes(d)
                            }
                        }
                        val line = inflater.inflate(R.layout.to_message, binding.cfl, false)
                        line.tol.toUsernameGroup.toUsername.setText(R.string.name_me)
                        line.tol.toMessage.text = binding.fctmsg.text
                        line.tol.toUsernameGroup.icon_fb2.setBackgroundResource(R.drawable.ic_girl_pic)
                        binding.cfl.addView(line)
                        binding.fctmsg.text.clear()
                    }?:mainWeakReference?.get()?.runOnUiThread {
                        Toast.makeText(context, "Please Login First", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    mainWeakReference?.get()?.runOnUiThread {
                        Toast.makeText(context, "${e.cause}: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
