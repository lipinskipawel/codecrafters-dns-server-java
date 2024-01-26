import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import static java.nio.ByteOrder.BIG_ENDIAN;
import static java.nio.charset.StandardCharsets.UTF_8;

public final class DnsMessage {

    public static byte[] header(short id, boolean qr) {
        return ByteBuffer.allocate(12)
                .order(BIG_ENDIAN)
                .putShort(id)
                .putShort((short) (qr ? 1 << 15 : 0))
                .putShort((short) 0x01) // qdcount
                .putShort((short) 0x01) // ancount
                .putShort((short) 0x00)
                .putShort((short) 0x00)
                .array();
    }

    public static byte[] encodeDomain(String domain) throws IOException {
        final var bytes = new ByteArrayOutputStream();
        for (var part : domain.split("\\.")) {
            bytes.write(part.length());
            bytes.write(part.getBytes(UTF_8));
        }
        bytes.write(0x00);
        return bytes.toByteArray();
    }
}
