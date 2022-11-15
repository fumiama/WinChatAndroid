package top.fumiama.winchatandroid

import org.junit.Assert.assertEquals
import org.junit.Test
import top.fumiama.winchatandroid.crc.CRC32

class CRC32UnitTest {
    @Test
    fun crc32_isCorrect() {
        assertEquals(1870807101, CRC32(0xacfededa.toInt()).crc32("i am wkk"))
    }
}
