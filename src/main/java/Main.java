import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import static java.lang.System.arraycopy;

public class Main {
    public static void main(String[] args) {
        try (DatagramSocket serverSocket = new DatagramSocket(2053)) {
            while (true) {
                final byte[] buf = new byte[512];
                final DatagramPacket packet = new DatagramPacket(buf, buf.length);
                serverSocket.receive(packet);
                System.out.println("Received data");

                final var header = DnsMessage.header((short) 1234, true);
                final var questionPacket = DnsQuestion.question("codecrafters.io", DnsTypes.Qtype.A, DnsTypes.Cclass.IN);
                final var answerPacket = DnsAnswer.answer("codecrafters.io", DnsTypes.Qtype.A, DnsTypes.Cclass.IN);

                final var bufResponse = new byte[header.length + questionPacket.length + answerPacket.length];
                arraycopy(header, 0, bufResponse, 0, header.length);
                arraycopy(questionPacket, 0, bufResponse, header.length, questionPacket.length);
                arraycopy(answerPacket, 0, bufResponse, header.length + questionPacket.length, answerPacket.length);

                final DatagramPacket packetResponse = new DatagramPacket(bufResponse, bufResponse.length, packet.getSocketAddress());
                serverSocket.send(packetResponse);
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }

    private static void printByteBuffer(byte[] bytes) {
        int counter = 0;
        for (byte b : bytes) {
            if (counter > 1) {
                System.out.println("");
                counter = 0;
            }
            System.out.print(Integer.toBinaryString(b) + " ");
            counter++;
        }
        System.out.println("");
    }
}
