import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class ByteManipulation {

    public static List<byte[]> domainName(byte[] dnsPacket) throws IOException {
        int numberOfQuestions = numberOfQuestions(dnsPacket);

        int start = 12;
        byte end = ByteManipulation.indexByteNull(dnsPacket, start);
        byte[] firstDomain = ByteManipulation.slice(dnsPacket, start, end);

        if (numberOfQuestions == 1) {
            return List.of(firstDomain);
        }

        final var domains = new ArrayList<byte[]>();
        domains.add(firstDomain);

        start = end + 5;
        byte[] readDomainParts = new byte[512];
        while (dnsPacket[start] != 0x00) {
            if (isPointer(dnsPacket[start])) {
                byte offsetFirst = (byte) offsetPosition(dnsPacket[start]);
                byte offsetSecond = dnsPacket[start + 1]; // I should masked it
                byte offset = (byte) (offsetFirst + offsetSecond);
                // read domain
                byte[] domainPart = ByteManipulation.slice(dnsPacket, offset, ByteManipulation.indexByteNull(dnsPacket, offset));
                // mark next domain
                domains.add(concat(removeZeroes(readDomainParts), domainPart));
                readDomainParts = new byte[512];
                start = start + 3;
            } else {
                // read length
                byte length = dnsPacket[start];
                // read domain part
                readDomainParts = ByteManipulation.slice(dnsPacket, start, start + length);
                start = start + length + 1;
            }
        }
        return domains;
    }

    public static byte[] concat(byte[] first, byte[] second) {
        final var res = new byte[first.length + second.length];
        System.arraycopy(first, 0, res, 0, first.length);
        System.arraycopy(second, 0, res, first.length, second.length);
        return res;
    }

    public static byte[] removeZeroes(byte[] bytes) {
        final var result = new ByteArrayOutputStream();
        for (byte b : bytes) {
            if (b != 0x00) {
                result.write(b);
            }
        }
        return result.toByteArray();
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

    public static int offsetPosition(int pointer) {
        short mask = 0xC0;
        int xor = mask ^ 0xFFFF;
        return pointer & xor;
    }
}
