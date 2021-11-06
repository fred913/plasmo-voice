package su.plo.voice.protocol.packets.udp;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import lombok.Data;
import su.plo.voice.protocol.packets.Packet;
import su.plo.voice.protocol.packets.PacketHelper;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;

@Data
public class MessageUdp {
    private static final PacketHelper packets = new PacketHelper();
    private static final int HEADER_LENGTH = 17;

    private final long ttl;
    private final long sequenceNumber;
    private final SocketAddress address;
    private final Packet payload;
    private final int payloadLength;

    private long timestamp;
    private long duration;

    private MessageUdp(long timestamp, long sequenceNumber, SocketAddress address, Packet payload, int payloadLength) {
        this.timestamp = timestamp;
        this.sequenceNumber = sequenceNumber;
        this.ttl = 2000L;
        this.address = address;
        this.payload = payload;
        this.payloadLength = payloadLength;
    }

    static {
        // audio packets
        packets.register(AudioDirectC2SPacket.class);
        packets.register(AudioDirectS2CPacket.class);
        packets.register(AudioPlayerC2SPacket.class);
        packets.register(AudioPlayerS2CPacket.class);
        packets.register(AudioRawC2SPacket.class);
        packets.register(AudioRawS2CPacket.class);
        packets.register(AudioSourceS2CPacket.class);

        // auth packets
        packets.register(AuthC2SPacket.class);
        packets.register(AuthS2CPacket.class);

        // ping packets
        packets.register(PingC2SPacket.class);
        packets.register(PingS2CPacket.class);
    }

    public static MessageUdp read(DatagramSocket socket) throws IOException {
        DatagramPacket datagram = new DatagramPacket(new byte[4096], 4096);
        socket.receive(datagram);

        ByteArrayDataInput in = ByteStreams.newDataInput(datagram.getData());
        Packet pkt = packets.byType(in.readByte());
        if (pkt != null) {
            long timestamp = in.readLong();
            long sequenceNumber = in.readLong();
            int packetLength = datagram.getData().length - HEADER_LENGTH;

            pkt.read(in);

            MessageUdp message = new MessageUdp(timestamp, sequenceNumber, datagram.getSocketAddress(), pkt, packetLength);
            ((AbstractPacketUdp) pkt).setMessage(message);

            return message;
        }

        return null;
    }

    /**
     * UDP packet:
     * byte - packet id
     * long - timestamp
     * long - sequence number
     * byte[] - payload
     */
    public static byte[] write(Packet packet, Long timestamp, long sequenceNumber) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        int type = packets.getType(packet);
        if (type < 0) {
            return null;
        }
        out.writeByte(type);
        out.writeLong(timestamp == null ? System.currentTimeMillis() : timestamp);
        out.writeLong(sequenceNumber);

        try {
            packet.write(out);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return out.toByteArray();
    }
}
