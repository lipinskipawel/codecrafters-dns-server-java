import java.io.IOException;
import java.nio.ByteBuffer;

import static java.nio.ByteOrder.BIG_ENDIAN;

public final class DnsAnswer {

    public static byte[] answer(byte[] received, DnsTypes.Qtype type, DnsTypes.Cclass cclass) throws IOException {
        final var domains = ByteManipulation.domainName(received).stream()
                .map(domainName -> ByteBuffer.allocate(domainName.length + 14)
                        .order(BIG_ENDIAN)
                        .put(domainName)
                        .putShort(type.getValue())
                        .putShort(cclass.getValue())
                        .putInt(0x3c)
                        .putShort((short) 0x04)
                        .put((byte) 0x08)
                        .put((byte) 0x08)
                        .put((byte) 0x08)
                        .put((byte) 0x08)
                        .array())
                .toList();
        final int length = domains.stream().map(it -> it.length).reduce(0, Integer::sum, Integer::sum);
        final var result = ByteBuffer.allocate(length);
        domains.forEach(result::put);
        return result.array();
    }
}
