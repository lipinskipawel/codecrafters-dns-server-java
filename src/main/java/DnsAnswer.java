import java.io.IOException;
import java.nio.ByteBuffer;

import static java.nio.ByteOrder.BIG_ENDIAN;

public final class DnsAnswer {

    public static byte[] answer(String domain, DnsTypes.Qtype type, DnsTypes.Cclass cclass) throws IOException {
        final var domainBytes = DnsHeader.encodeDomain(domain);
        return ByteBuffer.allocate(domainBytes.length + 14)
                .order(BIG_ENDIAN)
                .put(domainBytes)
                .putShort(type.getValue())
                .putShort(cclass.getValue())
                .putInt(0x36)
                .putShort((short) 0x04)
                .put((byte) 0x08)
                .put((byte) 0x08)
                .put((byte) 0x08)
                .put((byte) 0x08)
                .array();
    }
}
