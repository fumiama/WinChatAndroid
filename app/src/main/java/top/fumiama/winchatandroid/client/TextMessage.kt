package top.fumiama.winchatandroid.client

import java.nio.ByteBuffer
import java.nio.ByteOrder

class TextMessage(var fromID: Int, var toID: Int, var msg: String) {
    constructor(data: ByteArray): this(
        ByteBuffer.wrap(data, 0, 4).order(ByteOrder.BIG_ENDIAN).asReadOnlyBuffer().int,
        ByteBuffer.wrap(data, 4, 4).order(ByteOrder.BIG_ENDIAN).asReadOnlyBuffer().int,
        data.copyOfRange(10, 10+ByteBuffer.wrap(data, 8, 2).order(ByteOrder.BIG_ENDIAN).asReadOnlyBuffer().short).decodeToString(),
    )
    fun marshal(): ByteArray {
        val msgBA = msg.encodeToByteArray()
        val b = ByteArray(4+4+2+msgBA.size)
        ByteBuffer.wrap(b, 0, 4+4+2).order(ByteOrder.BIG_ENDIAN).putInt(fromID).putInt(toID).putShort(msgBA.size.toShort())
        System.arraycopy(msgBA, 0, b, 4+4+2, msgBA.size)
        return b
    }
    companion object {
        val Type = Command.CMD_TYPE_MSG_TXT
    }
}
