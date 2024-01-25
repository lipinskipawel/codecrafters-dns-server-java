import java.nio.ByteBuffer;
import java.util.BitSet;

import static java.nio.ByteOrder.BIG_ENDIAN;

public final class DnsMessage {

    public static byte[] header(short id, boolean qr) {
        final var bitSet = new BitSet(8);
        if (qr) {
            bitSet.flip(7);
        }
        short zero = (short) 0;

        return ByteBuffer.allocate(12)
                .order(BIG_ENDIAN)
                .putShort(id)
                .put(bitSet.toByteArray())
                .put((byte) 0)
                .putShort((short) 1) //qdcount
                .putShort(zero) // ancount
                .putShort(zero)
                .putShort(zero)
                .array();
    }
}
