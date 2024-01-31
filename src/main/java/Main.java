import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

// heavily inspired by https://github.com/TotoLastro/codecrafters-dns-server-java
// which is MIT licensed. 983b1c9f9714b370a20da7cb81ce40b3c6373f92
public class Main {

    public static void main(String[] args) {
        final var split = args[1].split(":", 2);
        final var resolverAddress = new InetSocketAddress(split[0], Integer.parseInt(split[1]));

        try (DatagramSocket serverSocket = new DatagramSocket(2053)) {
            while (true) {
                final byte[] buf = new byte[512];
                final DatagramPacket packet = new DatagramPacket(buf, buf.length);
                serverSocket.receive(packet);
                System.out.println("Received data");

                final var dnsMessage = DnsMessageDecoder.decode(buf);

                final var responses = new ArrayList<DnsMessage>();
                for (var question : dnsMessage.question().questions()) {
                    final var response = getResponseForQuestion(dnsMessage.header(), question, serverSocket, resolverAddress);
                    responses.add(response);
                }

                final var answers = responses.stream()
                        .flatMap(it -> it.answer().records().stream())
                        .toList();
                final var response = buildFinalResponse(dnsMessage.header(), dnsMessage.question(), new DnsMessage.DnsSectionAnswer(answers), responses);
                System.out.println("build final response: " + response);
                final var bufResponse = DnsMessageEncoder.encode(response);

                final var packetResponse = new DatagramPacket(bufResponse, bufResponse.length, packet.getSocketAddress());
                serverSocket.send(packetResponse);
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }

    private static DnsMessage getResponseForQuestion(
            DnsMessage.DnsSectionHeader headerQuestion,
            DnsMessage.DnsSectionQuestion.DnsQuestion question,
            DatagramSocket serverSocket,
            InetSocketAddress forwardAddress
    ) throws IOException {
        var message = new DnsMessage(
                cloneForOneQuestion(headerQuestion),
                new DnsMessage.DnsSectionQuestion(singletonList(question)),
                new DnsMessage.DnsSectionAnswer(emptyList())
        );
        System.out.println("Forward(" + forwardAddress + ") : " + message);
        final byte[] queryBuffer = DnsMessageEncoder.encode(message);
        DatagramPacket packet = new DatagramPacket(queryBuffer, queryBuffer.length, forwardAddress);
        serverSocket.send(packet);

        final byte[] responseBuffer = new byte[512];
        final DatagramPacket responseFromForward = new DatagramPacket(responseBuffer, responseBuffer.length);
        serverSocket.receive(responseFromForward);
        var responseMessage = DnsMessageDecoder.decode(responseFromForward.getData());
        System.out.println("Receive(" + responseFromForward.getSocketAddress() + ") : " + responseMessage);
        return responseMessage;
    }

    private static DnsMessage.DnsSectionHeader cloneForOneQuestion(DnsMessage.DnsSectionHeader header) {
        return new DnsMessage.DnsSectionHeader(
                new Random().nextInt(1, Short.MAX_VALUE * 2),
                DnsMessage.DnsSectionHeader.QueryOrResponse.QUERY,
                header.operationCode(),
                header.authoritativeAnswer(),
                header.truncation(),
                header.recursionDesired(),
                header.recursionAvailable(),
                header.reserved(),
                header.error(),
                1,
                0,
                0,
                0
        );
    }

    private static DnsMessage buildFinalResponse(
            DnsMessage.DnsSectionHeader headerSection,
            DnsMessage.DnsSectionQuestion question,
            DnsMessage.DnsSectionAnswer answer,
            List<DnsMessage> responses
    ) {
        return new DnsMessage(
                cloneWithSpecifiedAnswers(headerSection, responses),
                question,
                answer);
    }

    private static DnsMessage.DnsSectionHeader cloneWithSpecifiedAnswers(DnsMessage.DnsSectionHeader header, List<DnsMessage> answers) {
        var responseHeaders = answers.stream().map(DnsMessage::header).toList();
        return new DnsMessage.DnsSectionHeader(
                header.packetIdentifier(),
                DnsMessage.DnsSectionHeader.QueryOrResponse.RESPONSE,
                header.operationCode(),
                responseHeaders.stream().map(DnsMessage.DnsSectionHeader::authoritativeAnswer).findFirst().orElse(header.authoritativeAnswer()),
                responseHeaders.stream().map(DnsMessage.DnsSectionHeader::truncation).findFirst().orElse(header.truncation()),
                header.recursionDesired(),
                responseHeaders.stream().map(DnsMessage.DnsSectionHeader::recursionAvailable).findFirst().orElse(header.recursionAvailable()),
                responseHeaders.stream().map(DnsMessage.DnsSectionHeader::reserved).findFirst().orElse(header.reserved()),
                responseHeaders.stream().map(DnsMessage.DnsSectionHeader::error).findFirst().orElse(header.error()),
                header.questionCount(),
                (int) answers.stream().mapToLong(m -> m.answer().records().size()).sum(),
                header.nameserverCount(),
                header.additionalRecordCount()
        );
    }
}
