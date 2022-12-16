package top.fumiama.winchatandroid.crc;
import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;


/**
 * Generates a crc32 checksum for a given string or byte array
 */
public class CRC64 {
    private final static long polynomial = 0x4C11DB74C11DB7L;

    public CRC64() {}

    public long crc64(@NonNull String str) {
        return crc64(str.getBytes());
    }

    public long crc64(@NonNull byte[] data) {
        int index =0;
        long crc = 0xffffffff_ffffffffL;  // Initial value
        int length = data.length;
        while((length--) != 0) {
            crc ^= (long)(data[index++]) << 56;
            for (int i = 0; i < 8; i++) {
                if ( (crc & 0x80000000_00000000L ) != 0 )
                    crc = (crc << 1) ^ polynomial;
                else
                    crc <<= 1;
            }
        }
        return crc;
    }

    public long crc64(@NonNull File f) throws IOException {
        long crc = 0xffffffff_ffffffffL;  // Initial value
        long length = f.length();
        FileInputStream fi = new FileInputStream(f);
        byte[] b = new byte[1];
        while((length--) != 0) {
            if (fi.read(b) <= 0) break;
            crc ^= (long)(b[0]) << 56;
            for (int i = 0; i < 8; i++) {
                if ( (crc & 0x80000000_00000000L ) != 0 )
                    crc = (crc << 1) ^ polynomial;
                else
                    crc <<= 1;
            }
        }
        fi.close();
        return crc;
    }
}
