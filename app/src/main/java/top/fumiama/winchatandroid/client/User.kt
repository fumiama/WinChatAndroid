package top.fumiama.winchatandroid.client

import android.util.Log
import top.fumiama.winchatandroid.client.Command.Companion.CMD_TYPE_LOGIN
import top.fumiama.winchatandroid.crc.CRC32
import top.fumiama.winchatandroid.net.UDP
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.experimental.and

class User(val name: String, private val pwd: String) {
    var udp: UDP? = null
    private var id = 0
    private fun challenge(poly: UInt) = CRC32(poly.toInt()).crc32(pwd).toUInt()

    fun userID(): Int = id

    /**
     * @return 0: failed others: uid
    */
    fun login(s: UDP): Int {
        if(id != 0) return id
        val b = ByteArray(2048)
        b[0] = 0 // msg: login_username
        b[1] = name.length.toByte() // len
        val nb = name.encodeToByteArray()
        System.arraycopy(nb, 0, b, 2, nb.size) // username
        s.send(Command(CMD_TYPE_LOGIN, b).marshal(2+name.length))
        b[0] = 0
        s.recv(b)
        var cmd = Command(b)
        if (cmd.typ != CMD_TYPE_LOGIN) return 0 // invalid type
        if (cmd.data[0].toInt() != 1) return 0 // msg: 1: challenge, others: invalid
        val n = cmd.data[1].toInt().and(0xff)
        val bitslen = n/8 + if (n%8>0) 1 else 0
        val bits = ByteBuffer.wrap(cmd.data, 2+bitslen, cmd.data.size-2-bitslen).order(ByteOrder.BIG_ENDIAN).asReadOnlyBuffer()
        var sum = 0u
        var c = 0
        out_for@for (i in 0 until bitslen) {
            var p: Byte = 1
            for (j in 0..7) {
                if (c++ >= n) break@out_for
                val d = if (cmd.data[2+i].and(p).toUByte() > 0u) bits.int.toUInt() else bits.short.toUInt()
                Log.d("MyUser", "add: $d")
                sum += d
                p = p.rotateLeft(1)
            }
        }
        Log.d("MyUser", "challenge sum: $sum")
        sum = challenge(sum)
        Log.d("MyUser", "ack sum: $sum")
        b[0] = 1 // msg: challenge_ack
        ByteBuffer.wrap(b, 1, 4).order(ByteOrder.BIG_ENDIAN).putInt(sum.toInt())
        s.send(Command(CMD_TYPE_LOGIN, b).marshal(1+4))
        b[0] = 0
        s.recv(b)
        cmd = Command(b)
        if (cmd.typ != CMD_TYPE_LOGIN) return 0 // invalid type
        if (cmd.data[0].toInt() != 3) return 0 // msg: 3: login_success, others: invalid
        id = ByteBuffer.wrap(cmd.data, 1, 4).order(ByteOrder.BIG_ENDIAN).asReadOnlyBuffer().int
        udp = s
        return id
    }

    fun getCommand(): Command? {
        if(id == 0) return null
        val b = ByteArray(2048)
        return udp?.recv(b)?.let { Command(b) }
    }
}
