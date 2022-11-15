package top.fumiama.winchatandroid.crc;
import androidx.annotation.NonNull;

/**
 * Generates a crc32 checksum for a given string or byte array
 */
public class CRC32 {
    private final int polynomial;

    public CRC32(int poly) {
        polynomial = poly | 1; // keep lsb to be 1
    }

    public int crc32(@NonNull String str) {
        return crc32(str.getBytes());
    }

    public int crc32(@NonNull byte[] data) {
        int index =0;
        int crc = 0xffffffff;  // Initial value
        int length = data.length;
        while((length--) != 0) {
            crc ^= (int)(data[index++]) << 24;
            for (int i = 0; i < 8; i++) {
                if ( (crc & 0x80000000 ) != 0 )
                    crc = (crc << 1) ^ polynomial;
                else
                    crc <<= 1;
            }
        }
        return crc;
    }
}
