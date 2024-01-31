import java.util.Arrays;

public final class DnsTypes {

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

        public static Qtype qTypeFromShort(short qType) {
            return Arrays.stream(Qtype.values())
                    .filter(it -> it.value == qType)
                    .findFirst()
                    .orElseThrow();
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


        public static Cclass cClassFromShort(short clazz) {
            return Arrays.stream(Cclass.values())
                    .filter(it -> it.value == clazz)
                    .findFirst()
                    .orElseThrow();
        }

        public short getValue() {
            return value;
        }
    }
}
