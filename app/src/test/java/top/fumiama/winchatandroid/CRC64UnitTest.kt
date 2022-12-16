package top.fumiama.winchatandroid

import org.junit.Assert.assertEquals
import org.junit.Test
import top.fumiama.winchatandroid.crc.CRC32
import top.fumiama.winchatandroid.crc.CRC64

class CRC64UnitTest {
    @Test
    fun crc64_isCorrect() {
        assertEquals(8758698730011237488, CRC64().crc64("i am wkk"))
    }
}
