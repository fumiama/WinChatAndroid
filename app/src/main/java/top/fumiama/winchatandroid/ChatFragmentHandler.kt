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
            CHAT_F_MSG_INSERT_TO_FILE -> weakF.get()?.insertToFile(msg.data)
            CHAT_F_NAV_TO_FILE_F -> weakF.get()?.navigate2file()
            CHAT_F_NAV_TO_MEMBER_F -> weakF.get()?.navigate2members()
            CHAT_F_MSG_INSERT_FROM_FILE -> weakF.get()?.insertFromFile(msg.data)
        }
    }

    companion object {
        const val CHAT_F_MSG_INSERT_FROM_MSG = 1
        const val CHAT_F_MSG_INSERT_TO_FILE = 2
        const val CHAT_F_NAV_TO_FILE_F = 3
        const val CHAT_F_NAV_TO_MEMBER_F = 4
        const val CHAT_F_MSG_INSERT_FROM_FILE = 5
    }
}
