import java.io.IOException;
import java.nio.ByteBuffer;

import static java.nio.ByteOrder.BIG_ENDIAN;

public final class DnsQuestion {

    public static byte[] question(byte[] received, DnsTypes.Qtype qtype, DnsTypes.Cclass cclass) throws IOException {
        final var domains = ByteManipulation.domainName(received).stream()
                .map(domainName -> ByteBuffer.allocate(domainName.length + 4)
                        .order(BIG_ENDIAN)
                        .put(domainName)
                        .putShort(qtype.getValue())
                        .putShort(cclass.getValue())
                        .array())
                .toList();
        final int length = domains.stream().map(it -> it.length).reduce(0, Integer::sum, Integer::sum);
        final var result = ByteBuffer.allocate(length);
        domains.forEach(result::put);
        return result.array();
    }
}
