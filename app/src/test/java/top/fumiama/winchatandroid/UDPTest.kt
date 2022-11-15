package top.fumiama.winchatandroid

import org.junit.Assert
import org.junit.Test
import top.fumiama.winchatandroid.crc.CRC32
import top.fumiama.winchatandroid.net.UDP

class UDPTest {
    @Test
    fun udp_isCorrect() {
        UDP("113.54.212.240", 6666).send("hello world".encodeToByteArray())
    }
}
