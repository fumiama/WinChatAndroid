package top.fumiama.winchatandroid

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
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
            Thread {
                FriendListFragment.user = User(binding.flitun.text.toString(), binding.flitpwd.text.toString()).let {
                    context?.let { ctx ->
                        it.login(SettingsFragment.getUDP(ctx))
                    }
                    it
                }
            }.start()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
