package top.fumiama.winchatandroid

import android.os.Bundle
import android.os.Message
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.edit
import androidx.navigation.fragment.findNavController
import top.fumiama.winchatandroid.ChatFragment.Companion.chatFragmentHandler
import top.fumiama.winchatandroid.FriendListFragment.Companion.friendListFragmentHandler
import top.fumiama.winchatandroid.MainActivity.Companion.mainWeakReference
import top.fumiama.winchatandroid.client.Command
import top.fumiama.winchatandroid.client.Command.Companion.CMD_TYPE_GRP_JOIN
import top.fumiama.winchatandroid.client.Command.Companion.CMD_TYPE_GRP_QUIT
import top.fumiama.winchatandroid.client.Command.Companion.CMD_TYPE_MSG_TXT
import top.fumiama.winchatandroid.client.GroupJoinQuitReply
import top.fumiama.winchatandroid.client.TextMessage
import top.fumiama.winchatandroid.client.User
import top.fumiama.winchatandroid.databinding.FragmentLoginBinding
import top.fumiama.winchatandroid.net.UDP
import java.io.File

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.fltsgnup.setOnClickListener {
            findNavController().navigate(R.id.action_LoginFragment_to_RegisterFragment)
        }
        binding.flb.setOnClickListener {
            if (FriendListFragment.user != null) {
                findNavController().popBackStack()
            }
            if (binding.flitun.text.isEmpty() || binding.flitpwd.text.isEmpty()) {
                return@setOnClickListener
            }
            binding.flsld.visibility = View.VISIBLE
            Thread {
                context?.let { ctx ->
                    udp = SettingsFragment.getUDP(ctx)
                    if(udp == null) return@Thread
                    try {
                        FriendListFragment.user = User(binding.flitun.text.toString(), binding.flitpwd.text.toString()).let {
                            it.login(udp!!)
                            it
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        mainWeakReference?.get()?.runOnUiThread {
                            Toast.makeText(context, "${e.cause}: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                    }
                    if (FriendListFragment.user != null && FriendListFragment.user!!.userID() != 0) mainWeakReference?.get()?.runOnUiThread {
                        findNavController().popBackStack()
                        Toast.makeText(context, "登录成功, ID: ${FriendListFragment.user!!.userID()}", Toast.LENGTH_SHORT).show()
                    }
                    val msgFolder = mainWeakReference?.get()?.msgFolder ?: return@Thread
                    while (FriendListFragment.user != null && FriendListFragment.user!!.userID() != 0) {
                        FriendListFragment.user?.let userLet@ { user ->
                            user.getCommand(udp!!)?.let cmdLet@ { cmd ->
                                Log.d("MyLF", "received msg type: ${cmd.typ.typ}")
                                when(cmd.typ) {
                                    CMD_TYPE_MSG_TXT -> {
                                        val msgTxt = TextMessage(cmd.data)
                                        Log.d("MyLF", "received TextMessage, from: ${msgTxt.fromID}, to: ${msgTxt.toID}, msg: ${msgTxt.msg}")
                                        if(msgTxt.toID != user.userID()) return@userLet
                                        val data = Bundle()
                                        data.putInt("id", msgTxt.fromID)
                                        var txt = msgTxt.msg.substringBefore('\n').trim()
                                        if(txt.length > 32) txt = txt.take(32)
                                        data.putString("msg", txt)
                                        friendListFragmentHandler?.let {
                                            val msg = Message.obtain(it, FriendListFragmentHandler.FRIEND_LST_F_MSG_INSERT_ROW)
                                            msg.data = data
                                            msg.sendToTarget()
                                        }
                                        Log.d("MyLF", "lookup msg folder $msgFolder")
                                        File(msgFolder, "${msgTxt.fromID}").apply {
                                            Log.d("MyLF", "append bytes to $this")
                                            if(!exists()) createNewFile()
                                            setReadable(true)
                                            setWritable(true)
                                            setExecutable(false)
                                            appendBytes(cmd.marshal())
                                        }
                                        chatFragmentHandler?.let {
                                            val msgChat = Message.obtain(it, ChatFragmentHandler.CHAT_F_MSG_INSERT_FROM_MSG)
                                            msgChat.data = data
                                            msgChat.sendToTarget()
                                        }
                                    }
                                    CMD_TYPE_GRP_JOIN -> {
                                        val re = GroupJoinQuitReply(cmd.data)
                                        if(re.msg.toInt() != 0) {
                                            mainWeakReference?.get()?.runOnUiThread {
                                                Toast.makeText(context, "Join group ${re.grpID} failed", Toast.LENGTH_SHORT).show()
                                            }
                                            return@userLet
                                        }
                                        val data = Bundle()
                                        data.putInt("id", re.grpID)
                                        data.putString("msg", "create dialog")
                                        friendListFragmentHandler?.let {
                                            val msg = Message.obtain(it, FriendListFragmentHandler.FRIEND_LST_F_MSG_INSERT_ROW)
                                            msg.data = data
                                            msg.sendToTarget()
                                        }
                                    }
                                    CMD_TYPE_GRP_QUIT -> {
                                        val re = GroupJoinQuitReply(cmd.data)
                                        if(re.msg.toInt() != 0) mainWeakReference?.get()?.runOnUiThread {
                                            Toast.makeText(context, "Quit group ${re.grpID} failed", Toast.LENGTH_SHORT).show()
                                        }
                                        else mainWeakReference?.get()?.runOnUiThread {
                                            Toast.makeText(context, "Quit group ${re.grpID} succeeded", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    else -> {}
                                }
                            }
                        }
                    }
                }
            }.start()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        var udp: UDP? = null
    }
}
