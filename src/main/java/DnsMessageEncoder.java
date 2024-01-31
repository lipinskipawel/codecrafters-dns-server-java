import java.nio.ByteBuffer;

// heavily inspired by https://github.com/TotoLastro/codecrafters-dns-server-java
// which is MIT licensed. 983b1c9f9714b370a20da7cb81ce40b3c6373f92
public class DnsMessageEncoder {

    public static byte[] encode(DnsMessage message) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[512]);
        encodeHeaderSection(byteBuffer, message.header());
        encodeQuestionSection(byteBuffer, message.question());
        encodeAnswerSection(byteBuffer, message.answer());
        byte[] result = new byte[byteBuffer.position()];
        byteBuffer.rewind().get(result);
        return result;
    }

    private static void encodeHeaderSection(ByteBuffer byteBuffer, DnsMessage.DnsSectionHeader header) {
        byteBuffer.putShort((short) header.packetIdentifier());
        int byteToStore = header.queryOrResponse().value << 7;
        byteToStore |= (header.operationCode() << 3);
        byteToStore |= (header.authoritativeAnswer() << 2);
        byteToStore |= (header.truncation() << 1);
        byteToStore |= header.recursionDesired();
        byteBuffer.put((byte) byteToStore);
        byteToStore = header.recursionAvailable() << 7;
        byteToStore |= (header.reserved() << 4);
        byteToStore |= header.error();
        byteBuffer.put((byte) byteToStore);
        byteBuffer.putShort((short) header.questionCount());
        byteBuffer.putShort((short) header.answerCount());
        byteBuffer.putShort((short) header.nameserverCount());
        byteBuffer.putShort((short) header.additionalRecordCount());
    }

    private static void encodeQuestionSection(ByteBuffer byteBuffer, DnsMessage.DnsSectionQuestion questionSection) {
        for (DnsMessage.DnsSectionQuestion.DnsQuestion question : questionSection.questions()) {
            encodeLabels(byteBuffer, question.labels());
            byteBuffer.putShort(question.type().getValue());
            byteBuffer.putShort(question.clazz().getValue());
        }
    }

    private static void encodeLabels(ByteBuffer byteBuffer, String question) {
        for (String label : question.split("\\.")) {
            byteBuffer.put((byte) label.length());
            byteBuffer.put(label.getBytes());
        }
        byteBuffer.put((byte) 0);
    }

    private static void encodeAnswerSection(ByteBuffer byteBuffer, DnsMessage.DnsSectionAnswer answer) {
        for (DnsMessage.DnsSectionAnswer.DnsRecord record : answer.records()) {
            encodeLabels(byteBuffer, record.name());
            byteBuffer.putShort(record.dataType().getValue());
            byteBuffer.putShort(record.dataClass().getValue());
            byteBuffer.putInt(record.ttl());
            byteBuffer.putShort((short) record.data().length);
            byteBuffer.put(record.data());
        }
    }
}
