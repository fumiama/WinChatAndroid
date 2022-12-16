package top.fumiama.winchatandroid.client

import java.nio.ByteBuffer
import java.nio.ByteOrder

class GroupJoinQuitReply(var grpID: Int, var msg: Byte) {
    constructor(data: ByteArray): this(
        ByteBuffer.wrap(data, 0, 4).order(ByteOrder.BIG_ENDIAN).asReadOnlyBuffer().int,
        data[4]
    )
    fun marshal(): ByteArray {
        val b = ByteArray(5)
        ByteBuffer.wrap(b, 0, 4).order(ByteOrder.BIG_ENDIAN).putInt(grpID)
        b[4] = msg
        return b
    }
}
