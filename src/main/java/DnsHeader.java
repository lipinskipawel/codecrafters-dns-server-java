import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import static java.nio.ByteOrder.BIG_ENDIAN;
import static java.nio.charset.StandardCharsets.UTF_8;

public final class DnsHeader {

    private static final int OPCODE = 15 << 11;
    private static final int RD = 1 << 8;
    private static final int RCODE = 1 << 2;

    public static byte[] header(byte[] received) throws IOException {
        final var dataInputStream = new DataInputStream(new ByteArrayInputStream(received));

        final var id = dataInputStream.readShort();
        final int receivedSecondLine = dataInputStream.readShort();
        int secondLine = 1 << 15;

        secondLine = setBits(secondLine, receivedSecondLine & OPCODE);
        secondLine = setBits(secondLine, receivedSecondLine & RD);
        secondLine = setBits(secondLine, RCODE);

        return ByteBuffer.allocate(12)
                .order(BIG_ENDIAN)
                .putShort(id)
                .putShort((short) secondLine)
                .putShort((short) 0x01) // qdcount
                .putShort((short) 0x01) // ancount
                .putShort((short) 0x00)
                .putShort((short) 0x00)
                .array();
    }

    private static int setBits(int integer, int mask) {
        return integer | mask;
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
