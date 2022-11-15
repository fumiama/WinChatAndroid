package top.fumiama.winchatandroid.client

import java.nio.ByteBuffer
import java.nio.ByteOrder

class GroupListItem(var idOrCrc64: Long, var name: String) {
    constructor(data: ByteArray): this(
        ByteBuffer.wrap(data, 0, 8).order(ByteOrder.BIG_ENDIAN).asReadOnlyBuffer().long,
        data.copyOfRange(10, 10+ByteBuffer.wrap(data, 8, 2).order(ByteOrder.BIG_ENDIAN).asReadOnlyBuffer().short).decodeToString(),
    )
    fun marshal(): ByteArray {
        val msgBA = name.encodeToByteArray()
        val b = ByteArray(8+2+msgBA.size)
        ByteBuffer.wrap(b, 0, 8+2).order(ByteOrder.BIG_ENDIAN)
            .putLong(idOrCrc64)
            .putShort(msgBA.size.toShort())
        System.arraycopy(msgBA, 0, b, 8+2, msgBA.size)
        return b
    }
}
