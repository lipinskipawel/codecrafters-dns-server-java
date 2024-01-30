import java.io.IOException;
import java.nio.ByteBuffer;

import static java.nio.ByteOrder.BIG_ENDIAN;

public final class DnsQuestion {

    public static byte[] question(byte[] received, DnsTypes.Qtype qtype, DnsTypes.Cclass cclass) throws IOException {
        final var domainName = ByteManipulation.domainName(received);
        return ByteBuffer.allocate(domainName.length + 4)
                .order(BIG_ENDIAN)
                .put(domainName)
                .putShort(qtype.getValue())
                .putShort(cclass.getValue())
                .array();
    }
}
