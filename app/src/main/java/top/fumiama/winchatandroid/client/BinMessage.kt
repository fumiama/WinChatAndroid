package top.fumiama.winchatandroid.client

import java.nio.ByteBuffer
import java.nio.ByteOrder

class BinMessage(var fromID: Int, var toID: Int, var crc64: Long, var msg: String) {
    constructor(data: ByteArray): this(
        ByteBuffer.wrap(data, 0, 4).asReadOnlyBuffer().int,
        ByteBuffer.wrap(data, 4, 4).asReadOnlyBuffer().int,
        ByteBuffer.wrap(data, 8, 8).asReadOnlyBuffer().long,
        data.copyOfRange(18, 18+ByteBuffer.wrap(data, 16, 2).asReadOnlyBuffer().short).decodeToString(),
    )
    fun marshal(): ByteArray {
        val msgBA = msg.encodeToByteArray()
        val b = ByteArray(4+4+8+2+msgBA.size)
        ByteBuffer.wrap(b, 0, 4+4+8+2)
            .putInt(fromID)
            .putInt(toID)
            .putLong(crc64)
            .putShort(msgBA.size.toShort())
        System.arraycopy(msgBA, 0, b, 4+4+8+2, msgBA.size)
        return b
    }
    companion object {
        val Type = Command.CMD_TYPE_MSG_BIN
    }
}
