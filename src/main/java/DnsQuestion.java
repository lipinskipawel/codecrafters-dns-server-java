import java.io.IOException;
import java.nio.ByteBuffer;

import static java.nio.ByteOrder.BIG_ENDIAN;

public final class DnsQuestion {

    public static byte[] question(String domain, DnsTypes.Qtype qtype, DnsTypes.Cclass cclass) throws IOException {
        final var domainBytes = DnsHeader.encodeDomain(domain);
        return ByteBuffer.allocate(domainBytes.length + 4)
                .order(BIG_ENDIAN)
                .put(domainBytes)
                .putShort(qtype.getValue())
                .putShort(cclass.getValue())
                .array();
    }
}
