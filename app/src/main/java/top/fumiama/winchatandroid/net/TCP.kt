package top.fumiama.winchatandroid.net

import android.util.Log
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.net.*
import java.nio.ByteBuffer

class TCP(host: String, port: Int) {
    private val clientSocket = Socket(host, port)

    //普通交互流
    private var dout: OutputStream = clientSocket.getOutputStream()
    private var din: InputStream = clientSocket.getInputStream()

    init {
        clientSocket.soTimeout = 10000
    }

    fun send(file: File, crc64: Long) {
        val b = ByteArray(16)
        ByteBuffer.wrap(b, 0, 16).putLong(crc64).putLong(file.length())
        dout.write(b, 0, 16)
        Log.d("MyTCP", "send 16 bytes header")
        file.forEachBlock { buffer, bytesRead ->
            dout.write(buffer, 0, bytesRead)
        }
        dout.close()
    }
    fun recv() : ByteArray {
        val data = ByteArray(9)
        din.read(data)
        Log.d("MyTCP", "read 9 bytes")
        din.close()
        return data
    }
}
