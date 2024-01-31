import java.util.Arrays;
import java.util.List;
import java.util.Optional;

// heavily inspired by https://github.com/TotoLastro/codecrafters-dns-server-java
// which is MIT licensed. 983b1c9f9714b370a20da7cb81ce40b3c6373f92
public record DnsMessage(
        DnsSectionHeader header,
        DnsSectionQuestion question,
        DnsSectionAnswer answer
) {

    public record DnsSectionHeader(
            int packetIdentifier,
            QueryOrResponse queryOrResponse,
            int operationCode,
            int authoritativeAnswer,
            int truncation,
            int recursionDesired,
            int recursionAvailable,
            int reserved,
            int error,
            int questionCount,
            int answerCount,
            int nameserverCount,
            int additionalRecordCount
    ) {
        public enum QueryOrResponse {
            QUERY(0),
            RESPONSE(1);
            public final int value;

            QueryOrResponse(int value) {
                this.value = value;
            }

            public static Optional<QueryOrResponse> fromValue(int value) {
                return Arrays.stream(values())
                        .filter(type -> type.value == value)
                        .findFirst();
            }
        }
    }

    public record DnsSectionQuestion(List<DnsQuestion> questions) {
        public record DnsQuestion(String labels, DnsTypes.Qtype type, DnsTypes.Cclass clazz) {
        }
    }

    public record DnsSectionAnswer(List<DnsRecord> records) {
        public record DnsRecord(String name, DnsTypes.Qtype dataType, DnsTypes.Cclass dataClass, int ttl, byte[] data) {
        }
    }
}