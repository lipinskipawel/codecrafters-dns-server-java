import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

public final class ByteManipulation {

    public static byte[] domainName(byte[] dnsPacket) throws IOException {
        int numberOfQuestions = numberOfQuestions(dnsPacket);
        if (numberOfQuestions == 1) {
            byte start = 12;
            byte end = ByteManipulation.indexByteNull(dnsPacket, start);
            return ByteManipulation.slice(dnsPacket, start, end);
        }

        return null;
    }

    public static byte indexByteNull(byte[] bytes, int startPos) {
        for (int i = startPos; i < bytes.length; i++) {
            if (bytes[i] == 0x00) {
                return (byte) i;
            }
        }
        return (byte) bytes.length;
    }

    public static byte[] slice(byte[] source, int start, int endInclusive) {
        final var baos = new ByteArrayOutputStream();
        for (var i = start; i <= endInclusive; i++) {
            baos.write(source[i]);
        }
        return baos.toByteArray();
    }

    public static int numberOfQuestions(byte[] buf) throws IOException {
        byte[] qdCount = new byte[2];
        System.arraycopy(buf, 4, qdCount, 0, 2);
        return new DataInputStream(new ByteArrayInputStream(qdCount))
                .readShort();
    }

    public static boolean isPointer(byte octet) {
        short mask = 0xC0;

        int result = octet & mask;
        return result == mask;
    }
}
