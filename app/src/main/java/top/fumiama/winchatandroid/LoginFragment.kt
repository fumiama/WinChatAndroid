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
import androidx.preference.PreferenceManager
import top.fumiama.winchatandroid.ChatFragment.Companion.chatFragmentHandler
import top.fumiama.winchatandroid.FriendListFragment.Companion.friendListFragmentHandler
import top.fumiama.winchatandroid.MainActivity.Companion.mainWeakReference
import top.fumiama.winchatandroid.client.*
import top.fumiama.winchatandroid.client.Command.Companion.CMD_TYPE_GRP_JOIN
import top.fumiama.winchatandroid.client.Command.Companion.CMD_TYPE_GRP_LST
import top.fumiama.winchatandroid.client.Command.Companion.CMD_TYPE_GRP_QUIT
import top.fumiama.winchatandroid.client.Command.Companion.CMD_TYPE_MSG_BIN
import top.fumiama.winchatandroid.client.Command.Companion.CMD_TYPE_MSG_BIN_ACK
import top.fumiama.winchatandroid.client.Command.Companion.CMD_TYPE_MSG_TXT
import top.fumiama.winchatandroid.databinding.FragmentLoginBinding
import top.fumiama.winchatandroid.net.UDP
import top.fumiama.winchatandroid.ui.GroupListFragment.Companion.TYP_LST_ONLINE
import top.fumiama.winchatandroid.ui.GroupListFragment.Companion.groupListFragmentAdapterWeakReference
import java.io.File
import java.lang.Thread.sleep
import java.nio.ByteBuffer

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
            val ctx = requireContext()
            Thread {
                val udp = SettingsFragment.getUDP(ctx)
                try {
                    FriendListFragment.user = User(binding.flitun.text.toString(), binding.flitpwd.text.toString()).let {
                        it.login(udp)
                        it
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    mainWeakReference?.get()?.runOnUiThread {
                        Toast.makeText(ctx, "${e.cause}: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                }
                if (FriendListFragment.user != null && FriendListFragment.user!!.userID() != 0) mainWeakReference?.get()?.apply {
                    runOnUiThread {
                        findNavController().popBackStack()
                        Toast.makeText(ctx, String.format(getString(R.string.toast_login_success), FriendListFragment.user!!.userID()), Toast.LENGTH_SHORT).show()
                    }
                }
                val msgFolder = mainWeakReference?.get()?.msgFolder ?: return@Thread
                while (FriendListFragment.user != null && FriendListFragment.user!!.userID() != 0) {
                    FriendListFragment.user?.let userLet@ { user ->
                        user.getCommand()?.let cmdLet@ { cmd ->
                            Log.d("MyLF", "received msg type: ${cmd.typ.typ}")
                            when(cmd.typ) {
                                CMD_TYPE_MSG_TXT -> {
                                    val msgTxt = TextMessage(cmd.data)
                                    Log.d("MyLF", "received TextMessage, from: ${msgTxt.fromID}, to: ${msgTxt.toID}, msg: ${msgTxt.msg}")
                                    if(msgTxt.toID != user.userID()) return@userLet
                                    if(msgTxt.fromID == 0) {
                                        mainWeakReference?.get()?.apply { runOnUiThread { showNotice(msgTxt.msg) } }
                                        return@userLet
                                    }
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
                                        mainWeakReference?.get()?.apply {
                                            runOnUiThread {
                                                Toast.makeText(ctx, String.format(getString(R.string.toast_join_group_failed), re.grpID), Toast.LENGTH_SHORT).show()
                                            }
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
                                    if(re.msg.toInt() != 0) mainWeakReference?.get()?.apply {
                                        runOnUiThread {
                                            Toast.makeText(ctx, String.format(getString(R.string.toast_quit_group_failed), re.grpID), Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    else mainWeakReference?.get()?.apply {
                                        runOnUiThread {
                                            Toast.makeText(ctx, String.format(getString(R.string.toast_quit_group_success), re.grpID), Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                                CMD_TYPE_MSG_BIN_ACK -> {
                                    val ack = BinAckMessage(cmd.data)
                                    if(ack.port.toInt() == 0) {
                                        mainWeakReference?.get()?.runOnUiThread {
                                            Toast.makeText(ctx, R.string.toast_send_file_failed_server_refused, Toast.LENGTH_SHORT).show()
                                        }
                                        return@userLet
                                    }
                                    Log.d("MyLF", "get tcp port: ${ack.port}")
                                    SettingsFragment.getTCP(ctx, ack.port)?.let { tcp ->
                                        Log.d("MyLF", "got TCP")
                                        mainWeakReference?.get()?.cacheDir?.let { c ->
                                            File(c, "crc64${ack.crc64}").let { f ->
                                                Log.d("MyLF", "look file: $f, exist: ${f.exists()}")
                                                if (!f.exists()) {
                                                    mainWeakReference?.get()?.runOnUiThread {
                                                        Toast.makeText(ctx, R.string.toast_send_file_failed_file_not_found, Toast.LENGTH_SHORT).show()
                                                    }
                                                    return@userLet
                                                }
                                                Thread tcpThread@ {
                                                    try {
                                                        tcp.send(f, ack.crc64)
                                                        val b = tcp.recv()
                                                        if (ByteBuffer.wrap(b, 0, 8).asReadOnlyBuffer().long != ack.crc64 || b[8].toInt() != 0) {
                                                            mainWeakReference?.get()?.runOnUiThread {
                                                                Toast.makeText(ctx, R.string.toast_send_file_failed_server_error, Toast.LENGTH_SHORT).show()
                                                            }
                                                            return@tcpThread
                                                        }
                                                        val data = Bundle()
                                                        data.putLong("crc64", ack.crc64)
                                                        chatFragmentHandler?.let {
                                                            val msgChat = Message.obtain(it, ChatFragmentHandler.CHAT_F_MSG_INSERT_TO_FILE)
                                                            msgChat.data = data
                                                            msgChat.sendToTarget()
                                                        }
                                                    } catch (e: Exception) {
                                                        e.printStackTrace()
                                                        mainWeakReference?.get()?.runOnUiThread {
                                                            Toast.makeText(ctx, "${e.cause}: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                }.start()
                                            }
                                        }
                                    }
                                }
                                CMD_TYPE_GRP_LST -> {
                                    val grpList = GroupList(cmd.data)
                                    if (grpList.typ == TYP_LST_ONLINE) {
                                        Thread {
                                            grpList.items.forEach { item ->
                                                sleep(1000)
                                                friendListFragmentHandler?.let {
                                                    val data = Bundle()
                                                    data.putInt("id", item.idOrCrc64.toInt())
                                                    data.putString("name", item.name)
                                                    data.putString("msg", "online user")
                                                    val msg = Message.obtain(it, FriendListFragmentHandler.FRIEND_LST_F_MSG_INSERT_ROW)
                                                    msg.data = data
                                                    msg.sendToTarget()
                                                }
                                            }
                                            mainWeakReference?.get()?.apply {
                                                runOnUiThread {
                                                    Toast.makeText(this, String.format(getString(R.string.toast_online_user_count), grpList.items.size), Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }.start()
                                        return@userLet
                                    }
                                    var count = 1
                                    groupListFragmentAdapterWeakReference?.get()?.let { ad ->
                                        grpList.items.forEach {
                                            ad.idDataMap[count] = arrayOf(it.name, it.idOrCrc64.toString(), "0")
                                            count++
                                        }
                                        ad.refresh()
                                    }
                                }
                                CMD_TYPE_MSG_BIN -> {
                                    val msgBin = BinMessage(cmd.data)
                                    Log.d("MyLF", "received BinMessage, from: ${msgBin.fromID}, to: ${msgBin.toID}, crc64: ${msgBin.crc64}")
                                    if(msgBin.toID != user.userID() || msgBin.fromID == 0) return@userLet
                                    val data = Bundle()
                                    data.putInt("id", msgBin.fromID)
                                    data.putLong("crc64", msgBin.crc64)
                                    data.putString("msg", msgBin.msg)
                                    friendListFragmentHandler?.let {
                                        val msg = Message.obtain(it, FriendListFragmentHandler.FRIEND_LST_F_MSG_INSERT_ROW)
                                        msg.data = data
                                        msg.sendToTarget()
                                    }
                                    Log.d("MyLF", "lookup msg folder $msgFolder")
                                    File(msgFolder, "${msgBin.fromID}").apply {
                                        Log.d("MyLF", "append bytes to $this")
                                        if(!exists()) createNewFile()
                                        setReadable(true)
                                        setWritable(true)
                                        setExecutable(false)
                                        appendBytes(cmd.marshal())
                                    }
                                    chatFragmentHandler?.let {
                                        val msgChat = Message.obtain(it, ChatFragmentHandler.CHAT_F_MSG_INSERT_FROM_FILE)
                                        msgChat.data = data
                                        msgChat.sendToTarget()
                                    }
                                }
                                else -> {}
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
}
