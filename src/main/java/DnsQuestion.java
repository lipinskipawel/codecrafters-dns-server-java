import java.io.IOException;
import java.nio.ByteBuffer;

import static java.nio.ByteOrder.BIG_ENDIAN;
import static java.nio.charset.StandardCharsets.UTF_8;

public final class DnsQuestion {

    public static byte[] question(String domain, DnsQuestion.Qtype qtype, DnsQuestion.Cclass cclass) throws IOException {
        final var split = domain.split("\\.");
        return ByteBuffer.allocate(21)
                .order(BIG_ENDIAN)
                .put((byte) split[0].length())
                .put(split[0].getBytes(UTF_8))
                .put((byte) split[1].length())
                .put(split[1].getBytes(UTF_8))
                .put((byte) 0)
                .putShort(qtype.getValue())
                .putShort(cclass.getValue())
                .array();
    }

    // https://datatracker.ietf.org/doc/html/rfc1035#section-3.2.2
    public enum Qtype {
        A((short) 1),
        NS((short) 2),
        MD((short) 3),
        MF((short) 4),
        CNAME((short) 5),
        SOA((short) 6),
        MB((short) 7),
        MG((short) 8),
        MR((short) 9),
        NULL((short) 10),
        WKS((short) 11),
        PTR((short) 12),
        HINFO((short) 13),
        MINFO((short) 14),
        MX((short) 15),
        TXT((short) 16);

        private final short value;

        Qtype(short value) {
            this.value = value;
        }

        public short getValue() {
            return value;
        }
    }

    // https://datatracker.ietf.org/doc/html/rfc1035#section-3.2.4
    public enum Cclass {
        IN((short) 1),
        CS((short) 2),
        CH((short) 3),
        HS((short) 4);

        private final short value;

        Cclass(short value) {
            this.value = value;
        }

        public short getValue() {
            return value;
        }
    }
}
