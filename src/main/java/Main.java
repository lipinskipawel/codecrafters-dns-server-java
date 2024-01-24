import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.util.BitSet;

import static java.nio.ByteOrder.BIG_ENDIAN;

public class Main {
    public static void main(String[] args) {
        try (DatagramSocket serverSocket = new DatagramSocket(2053)) {
            while (true) {
                final byte[] buf = new byte[512];
                final DatagramPacket packet = new DatagramPacket(buf, buf.length);
                serverSocket.receive(packet);
                System.out.println("Received data");

                short id = (short) 1234;
                final var bitSet = new BitSet(8);
                bitSet.flip(7);
                short zero = (short) 0;
                final var bufResponse = ByteBuffer.allocate(12)
                        .order(BIG_ENDIAN)
                        .putShort(id)
                        .put(bitSet.toByteArray())
                        .put((byte) 0)
                        .putShort(zero)
                        .putShort(zero)
                        .putShort(zero)
                        .putShort(zero)
                        .array();

                final DatagramPacket packetResponse = new DatagramPacket(bufResponse, bufResponse.length, packet.getSocketAddress());
                serverSocket.send(packetResponse);
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }
}
