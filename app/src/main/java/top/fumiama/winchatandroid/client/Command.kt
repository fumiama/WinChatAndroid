package top.fumiama.winchatandroid.client

import java.nio.ByteBuffer
import java.nio.ByteOrder

class Command(var typ: CommandType, var data: ByteArray) {
    constructor (data: ByteArray) : this(when(data[0].toInt()) {
        0 -> CMD_TYPE_UNDEF
        1 -> CMD_TYPE_LOGIN
        2 -> CMD_TYPE_MSG_TXT
        3 -> CMD_TYPE_MSG_BIN
        4 -> CMD_TYPE_MSG_BIN_ACK
        5 -> CMD_TYPE_GRP_JOIN
        6 -> CMD_TYPE_GRP_QUIT
        7 -> CMD_TYPE_BIN_GET
        8 -> CMD_TYPE_GRP_LST
        else -> throw IndexOutOfBoundsException("Type")
    }, data.copyOfRange(3, 3+ByteBuffer.wrap(data, 1, 2).order(ByteOrder.BIG_ENDIAN).short.toInt()))
    fun marshal(): ByteArray {
        val d = ByteArray(3+data.size)
        ByteBuffer.wrap(d, 0, 3).put(typ.typB).putShort(data.size.toShort())
        System.arraycopy(data, 0, d, 3, data.size)
        return d
    }
    fun marshal(dataLen: Int): ByteArray {
        val d = ByteArray(3+dataLen)
        ByteBuffer.wrap(d, 0, 3).put(typ.typB).putShort(dataLen.toShort())
        System.arraycopy(data, 0, d, 3, dataLen)
        return d
    }
    companion object {
        val CMD_TYPE_UNDEF = CommandType(0)
        val CMD_TYPE_LOGIN = CommandType(1)
        val CMD_TYPE_MSG_TXT = CommandType(2)
        val CMD_TYPE_MSG_BIN = CommandType(3)
        val CMD_TYPE_MSG_BIN_ACK = CommandType(4)
        val CMD_TYPE_GRP_JOIN = CommandType(5)
        val CMD_TYPE_GRP_QUIT = CommandType(6)
        val CMD_TYPE_BIN_GET = CommandType(7)
        val CMD_TYPE_GRP_LST = CommandType(8)
    }
}
