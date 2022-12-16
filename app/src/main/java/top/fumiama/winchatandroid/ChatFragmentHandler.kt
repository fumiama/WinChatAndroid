package top.fumiama.winchatandroid

import android.os.Handler
import android.os.Looper
import android.os.Message
import java.lang.ref.WeakReference

class ChatFragmentHandler(fragment: ChatFragment, looper: Looper): Handler(looper) {
    private val weakF = WeakReference(fragment)

    override fun handleMessage(msg: Message) {
        super.handleMessage(msg)
        when(msg.what) {
            CHAT_F_MSG_INSERT_FROM_MSG -> weakF.get()?.insertFromMessage(msg.data)
        }
    }

    companion object {
        const val CHAT_F_MSG_INSERT_FROM_MSG = 1
    }
}
