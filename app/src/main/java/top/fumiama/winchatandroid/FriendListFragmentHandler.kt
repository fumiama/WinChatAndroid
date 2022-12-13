package top.fumiama.winchatandroid

import android.os.Handler
import android.os.Looper
import android.os.Message
import java.lang.ref.WeakReference

class FriendListFragmentHandler(fragment: FriendListFragment, looper: Looper): Handler(looper) {
    private val weakF = WeakReference(fragment)

    override fun handleMessage(msg: Message) {
        super.handleMessage(msg)
        when(msg.what) {
            FRIEND_LST_F_MSG_INSERT_ROW -> weakF.get()?.insertRow(msg.data)
        }
    }

    companion object {
        const val FRIEND_LST_F_MSG_INSERT_ROW = 1
    }
}
