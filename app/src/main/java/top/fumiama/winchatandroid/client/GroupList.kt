package top.fumiama.winchatandroid.client

import java.nio.ByteBuffer
import java.nio.ByteOrder

class GroupList(var typ: Byte, var groupID: Int, var items: Array<GroupListItem>) {
    constructor(data: ByteArray): this(
        data[0],
        ByteBuffer.wrap(data, 1, 4).asReadOnlyBuffer().int,
        data.let {
            val len = ByteBuffer.wrap(data, 5, 2).asReadOnlyBuffer().short
            var items = arrayOf<GroupListItem>()
            var p = 0
            for(i in 0 until len) {
                val dataLen = 8+2+ByteBuffer.wrap(data, p+8, 2).asReadOnlyBuffer().short.toInt()
                items += GroupListItem(data.copyOfRange(p, p+dataLen))
                p += dataLen
            }
            items
        },
    )
    fun marshal(): ByteArray {
        var totalSize = 1+4+2
        items.forEach {
            totalSize += 8+2+it.name.length
        }
        val b = ByteArray(totalSize)
        ByteBuffer.wrap(b, 0, 1+4+2)
            .put(typ)
            .putInt(groupID)
            .putShort(items.size.toShort())
        totalSize = 1+4+2
        items.forEach {
            val d = 8+2+it.name.length
            System.arraycopy(it.marshal(), 0, b, totalSize, d)
            totalSize += d
        }
        return b
    }
    companion object {
        val Type = Command.CMD_TYPE_GRP_LST
    }
}
