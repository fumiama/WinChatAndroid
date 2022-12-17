package top.fumiama.winchatandroid.client

import java.nio.ByteBuffer
import java.nio.ByteOrder

class GroupJoinQuit(var grpID: Int) {
    constructor(data: ByteArray): this(
        ByteBuffer.wrap(data, 0, 4).asReadOnlyBuffer().int,
    )
    fun marshal(): ByteArray {
        val b = ByteArray(4)
        ByteBuffer.wrap(b, 0, 4).putInt(grpID)
        return b
    }
}
