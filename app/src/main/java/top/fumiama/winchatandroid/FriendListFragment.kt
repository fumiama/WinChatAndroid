package top.fumiama.winchatandroid

import android.os.Bundle
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import top.fumiama.winchatandroid.client.User
import top.fumiama.winchatandroid.databinding.FragmentFriendlistBinding

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FriendListFragment : Fragment() {

    private var _binding: FragmentFriendlistBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFriendlistBinding.inflate(inflater, container, false)
        friend_lst_f_handler = FriendListFragmentHandler(this, Looper.myLooper()!!)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ffsw.setOnRefreshListener {
            if(user == null) { // 未登录
                binding.root.postDelayed({
                    findNavController().navigate(R.id.action_FriendListFragment_to_LoginFragment)
                }, 233)
            }
            binding.ffsw.isRefreshing = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun insertRow(b: Bundle) {
        val name = b.getString("name", "N/A")
        val msg = b.getString("msg", "N/A")
        val count = b.getInt("count", 0)
        val total = b.getInt("total", 0)

    }

    companion object {
        var user: User? = null
        var friend_lst_f_handler: FriendListFragmentHandler? = null
    }
}
