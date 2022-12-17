package top.fumiama.winchatandroid.client

import java.nio.ByteBuffer
import java.nio.ByteOrder

class BinAckMessage(var crc64: Long, var port: Short) {
    constructor(data: ByteArray): this(
        ByteBuffer.wrap(data, 0, 8).asReadOnlyBuffer().long,
        ByteBuffer.wrap(data, 8, 2).asReadOnlyBuffer().short,
    )
    fun marshal(): ByteArray {
        val b = ByteArray(8+2)
        ByteBuffer.wrap(b, 0, 8+2)
            .putLong(crc64)
            .putShort(port)
        return b
    }
    companion object {
        val Type = Command.CMD_TYPE_MSG_BIN_ACK
    }
}
