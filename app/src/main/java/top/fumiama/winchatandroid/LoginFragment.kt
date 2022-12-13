package top.fumiama.winchatandroid

import android.os.Bundle
import android.os.Message
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import top.fumiama.winchatandroid.FriendListFragment.Companion.friendListFragmentHandler
import top.fumiama.winchatandroid.MainActivity.Companion.mainWeakReference
import top.fumiama.winchatandroid.client.Command
import top.fumiama.winchatandroid.client.Command.Companion.CMD_TYPE_MSG_TXT
import top.fumiama.winchatandroid.client.TextMessage
import top.fumiama.winchatandroid.client.User
import top.fumiama.winchatandroid.databinding.FragmentLoginBinding

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
            Thread {
                context?.let { ctx ->
                    val udp = SettingsFragment.getUDP(ctx)
                    try {
                        FriendListFragment.user = User(binding.flitun.text.toString(), binding.flitpwd.text.toString()).let {
                            it.login(udp)
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
                    while (FriendListFragment.user != null && FriendListFragment.user!!.userID() != 0) {
                        FriendListFragment.user?.let userLet@ { user ->
                            user.getCommand(udp)?.let cmdLet@ { cmd ->
                                Log.d("MyLF", "received msg type: ${cmd.typ.typ}")
                                when(cmd.typ) {
                                    CMD_TYPE_MSG_TXT -> {
                                        val msgTxt = TextMessage(cmd.data)
                                        Log.d("MyLF", "received TextMessage, from: ${msgTxt.fromID}, to: ${msgTxt.toID}, msg: ${msgTxt.msg}")
                                        if(msgTxt.toID != user.userID()) return@userLet
                                        val msg = Message.obtain(friendListFragmentHandler, FriendListFragmentHandler.FRIEND_LST_F_MSG_INSERT_ROW)
                                        val data = Bundle()
                                        data.putInt("id", msgTxt.fromID)
                                        data.putString("msg", msgTxt.msg)
                                        msg.data = data
                                        msg.sendToTarget()
                                    }
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
}
