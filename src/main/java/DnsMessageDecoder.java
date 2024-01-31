import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

// heavily inspired by https://github.com/TotoLastro/codecrafters-dns-server-java
// which is MIT licensed. 983b1c9f9714b370a20da7cb81ce40b3c6373f92
public class DnsMessageDecoder {

    public static DnsMessage decode(byte[] buffer) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
        DnsMessage.DnsSectionHeader header = decodeHeader(byteBuffer);
        DnsMessage.DnsSectionQuestion question = decodeQuestion(byteBuffer, header.questionCount());
        DnsMessage.DnsSectionAnswer answer = decodeAnswer(byteBuffer, header.answerCount());
        return new DnsMessage(header, question, answer);
    }

    private static DnsMessage.DnsSectionHeader decodeHeader(ByteBuffer byteBuffer) {
        final int packetIdentifier = byteBuffer.getShort() & 0xFFFF;
        byte firstBitMask = byteBuffer.get();
        final int questionOrReponse = (firstBitMask >> 7) & 1;
        final int operationCode = (firstBitMask >> 3) & 0b1111;
        final int authoritativeAnswer = (firstBitMask >> 2) & 1;
        final int truncation = (firstBitMask >> 1) & 1;
        final int recursionDesired = firstBitMask & 1;
        byte secondBitMask = byteBuffer.get();
        final int recursionAvailable = (secondBitMask >> 7) & 1;
        final int reserved = (secondBitMask >> 4) & 0b111;
        final int error = secondBitMask & 0b1111;
        final int questionCount = byteBuffer.getShort();
        final int answerCount = byteBuffer.getShort();
        final int nameserverCount = byteBuffer.getShort();
        final int additionalRecordCount = byteBuffer.getShort();
        return new DnsMessage.DnsSectionHeader(
                packetIdentifier,
                DnsMessage.DnsSectionHeader.QueryOrResponse.fromValue(questionOrReponse).orElseThrow(),
                operationCode,
                authoritativeAnswer,
                truncation,
                recursionDesired,
                recursionAvailable,
                reserved,
                error,
                questionCount,
                answerCount,
                nameserverCount,
                additionalRecordCount
        );
    }

    private static DnsMessage.DnsSectionQuestion decodeQuestion(ByteBuffer byteBuffer, int numberOfQuestions) {
        return new DnsMessage.DnsSectionQuestion(
                IntStream.range(0, numberOfQuestions)
                        .mapToObj(i -> {
                            String labels = decodeLabels(byteBuffer);
                            final short queryType = byteBuffer.getShort();
                            final short queryClass = byteBuffer.getShort();
                            return new DnsMessage.DnsSectionQuestion.DnsQuestion(
                                    labels,
                                    DnsTypes.Qtype.qTypeFromShort(queryType),
                                    DnsTypes.Cclass.cClassFromShort(queryClass)
                            );
                        }).collect(Collectors.toList())
        );
    }

    private static String decodeLabels(ByteBuffer byteBuffer) {
        List<String> labels = new ArrayList<>();
        int labelLength;
        do {
            labelLength = byteBuffer.get() & 0b11111111;
            if ((labelLength >> 6) == 0b11) {
                int position = ((labelLength & 0b00111111) << 8) | (byteBuffer.get() & 0b11111111);
                labels.add(decodeLabels(byteBuffer.duplicate().position(position)));
            } else if (0 < labelLength) {
                String label = new String(byteBuffer.array(), byteBuffer.position(), labelLength, StandardCharsets.UTF_8);
                byteBuffer.position(byteBuffer.position() + label.length());
                labels.add(label);
            }
        } while (0 < labelLength && (labelLength >> 6) != 0b11);

        return String.join(".", labels);
    }

    private static DnsMessage.DnsSectionAnswer decodeAnswer(ByteBuffer byteBuffer, int numberOfAnswers) {
        List<DnsMessage.DnsSectionAnswer.DnsRecord> records = IntStream.range(0, numberOfAnswers)
                .mapToObj(i -> {
                    String labels = decodeLabels(byteBuffer);
                    short queryType = byteBuffer.getShort();
                    short queryClass = byteBuffer.getShort();
                    int ttl = byteBuffer.getInt();
                    int dataLength = byteBuffer.getShort();
                    byte[] data = new byte[dataLength];
                    byteBuffer.get(data);
                    return new DnsMessage.DnsSectionAnswer.DnsRecord(
                            labels,
                            DnsTypes.Qtype.qTypeFromShort(queryType),
                            DnsTypes.Cclass.cClassFromShort(queryClass),
                            ttl,
                            data
                    );
                })
                .collect(Collectors.toList());
        return new DnsMessage.DnsSectionAnswer(records);
    }
}
