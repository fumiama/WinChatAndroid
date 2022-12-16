package top.fumiama.winchatandroid

import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.from_message.view.*
import kotlinx.android.synthetic.main.to_message.view.*
import top.fumiama.winchatandroid.FriendListFragment.Companion.user
import top.fumiama.winchatandroid.MainActivity.Companion.mainWeakReference
import top.fumiama.winchatandroid.client.BinMessage
import top.fumiama.winchatandroid.client.Command
import top.fumiama.winchatandroid.client.Command.Companion.CMD_TYPE_MSG_BIN
import top.fumiama.winchatandroid.client.Command.Companion.CMD_TYPE_MSG_TXT
import top.fumiama.winchatandroid.client.TextMessage
import top.fumiama.winchatandroid.crc.CRC64
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
                            CMD_TYPE_MSG_BIN -> {
                                val binMsg = BinMessage(cmd.data)
                                Log.d("MyCF", "load bin msg from ${binMsg.fromID}, to ${binMsg.toID}, crc64: ${binMsg.msg}")
                                val line = inflater.inflate(if(binMsg.fromID == fromID) R.layout.from_message else R.layout.to_message, binding.cfl, false)
                                context?.let { ctx ->
                                    if(binMsg.fromID == fromID) {
                                        line.frl.fromUsernameGroup.fromUsername.text = name
                                        line.frl.fromMessage.text = PreferenceManager.getDefaultSharedPreferences(ctx).getString("crc64name${binMsg.crc64}", null)?:"N/A"
                                        line.frl.fromUsernameGroup.icon_fb1.setBackgroundResource(R.drawable.ic_girlz_pic)
                                        line.frl.fromMessage.setOnClickListener {
                                            mainWeakReference?.get()?.runOnUiThread {
                                                Toast.makeText(context, "stub!", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    } else {
                                        line.tol.toUsernameGroup.toUsername.setText(R.string.name_me)
                                        line.tol.toMessage.text = PreferenceManager.getDefaultSharedPreferences(ctx).getString("crc64name${binMsg.crc64}", null)?:"N/A"
                                        line.tol.toUsernameGroup.icon_fb2.setBackgroundResource(R.drawable.ic_girl_pic)
                                        line.tol.toMessage.setOnClickListener {

                                        }
                                    }
                                }

                                binding.cfl.addView(line)
                                data = data.copyOfRange(cmd.length, data.size)
                            }
                        }
                    }
                }
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        chatFragmentHandler = ChatFragmentHandler(this, Looper.myLooper()!!)
        arguments?.apply {
            val fromID = getInt("id", 0)
            binding.fcbsnd.setOnClickListener {
                Log.d("MyCF", "msg to send: ${binding.fctmsg.text}")
                if(binding.fctmsg.text.isEmpty()) return@setOnClickListener
                try {
                    user?.apply {
                        val d = Command(CMD_TYPE_MSG_TXT, TextMessage(userID(), fromID, binding.fctmsg.text.toString()).marshal()).marshal()
                        Thread {
                            try {
                                udp?.send(d)
                            } catch (e: Exception) {
                                e.printStackTrace()
                                mainWeakReference?.get()?.runOnUiThread {
                                    Toast.makeText(context, "${e.cause}: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }.start()
                        mainWeakReference?.get()?.msgFolder?.apply {
                            File(this, "$fromID").apply {
                                if(!exists()) createNewFile()
                                appendBytes(d)
                            }
                            Log.d("MyCF", "write msg to file")
                        }
                        val line = layoutInflater.inflate(R.layout.to_message, binding.cfl, false)
                        line.tol.toUsernameGroup.toUsername.setText(R.string.name_me)
                        line.tol.toMessage.text = binding.fctmsg.text
                        line.tol.toUsernameGroup.icon_fb2.setBackgroundResource(R.drawable.ic_girl_pic)
                        binding.cfl.addView(line)
                        Log.d("MyCF", "inflate line")
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
            binding.fcbsnd.setOnLongClickListener {
                sendFileCallBack = { inputFile -> // already in another thread
                    try {
                        val crc64 = CRC64().crc64(inputFile)
                        mainWeakReference?.get()?.apply {
                            inputFile.copyTo(File(cacheDir, "crc64$crc64"), true)
                        }
                        context?.let { ctx ->
                            PreferenceManager.getDefaultSharedPreferences(ctx).edit {
                                putString("crc64name$crc64", "${inputFile.name}.${inputFile.extension}")
                                apply()
                            }
                        }
                        user?.udp?.apply {
                            val d = Command(Command.CMD_TYPE_MSG_BIN, BinMessage(user!!.userID(), fromID, crc64, "${inputFile.name}.${inputFile.extension}").marshal()).marshal()
                            mainWeakReference?.get()?.msgFolder?.apply {
                                File(this, "$fromID").apply {
                                    if(!exists()) createNewFile()
                                    appendBytes(d)
                                }
                                Log.d("MyCF", "write bin msg to file")
                            }
                            send(d)
                        }
                        val line = layoutInflater.inflate(R.layout.to_message, binding.cfl, false)
                        line.tol.toUsernameGroup.toUsername.setText(R.string.name_me)
                        line.tol.toMessage.text = inputFile.name
                        line.tol.toUsernameGroup.icon_fb2.setBackgroundResource(R.drawable.ic_girl_pic)
                        mainWeakReference?.get()?.runOnUiThread {
                            binding.cfl.addView(line)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        mainWeakReference?.get()?.runOnUiThread {
                            Toast.makeText(context, "${e.cause}: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                val i = Intent(Intent.ACTION_GET_CONTENT)
                i.type = "*/*"
                mainWeakReference?.get()?.fileSelector?.launch(i)
                return@setOnLongClickListener true
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        chatFragmentHandler = null
    }

    fun insertFromMessage(bundle: Bundle) {
        val fromID = bundle.getInt("id", 0)
        if(fromID != arguments?.getInt("id", -1)) return
        val line = layoutInflater.inflate(R.layout.from_message, binding.cfl, false)
        line.frl.fromUsernameGroup.fromUsername.text = fromID.toString()
        line.frl.fromMessage.text = bundle.getString("msg", "N/A")
        line.frl.fromUsernameGroup.icon_fb1.setBackgroundResource(R.drawable.ic_girlz_pic)
        binding.cfl.addView(line)
    }

    fun insertToFile(bundle: Bundle) {
        val crc64 = bundle.getLong("crc64", 0)
        context?.let { ctx ->
            (PreferenceManager.getDefaultSharedPreferences(ctx).getString("crc64name$crc64", null)?:"N/A").let { name ->
                val line = layoutInflater.inflate(R.layout.to_message, binding.cfl, false)
                line.tol.toUsernameGroup.toUsername.setText(R.string.name_me)
                line.tol.toMessage.text = name
                line.tol.toUsernameGroup.icon_fb2.setBackgroundResource(R.drawable.ic_girl_pic)
                binding.cfl.addView(line)
            }
        }
    }

    companion object {
        var chatFragmentHandler: ChatFragmentHandler? = null
        var sendFileCallBack: (File)->Unit = {}
    }
}
