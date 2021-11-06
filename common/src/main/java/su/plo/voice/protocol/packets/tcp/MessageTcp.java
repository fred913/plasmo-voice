package su.plo.voice.protocol.packets.tcp;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import su.plo.voice.protocol.packets.Packet;
import su.plo.voice.protocol.packets.PacketHelper;

import java.io.IOException;

public class MessageTcp {
    private static final PacketHelper packets = new PacketHelper();

    static {
        // connection packets
        packets.register(ConnectS2CPacket.class);
        packets.register(ConnectC2SPacket.class);

        // server packets
        packets.register(ConfigS2CPacket.class);
        packets.register(PermissionsS2CPacket.class);
        packets.register(ClientsListS2CPacket.class);
        packets.register(ClientMutedS2CPacket.class);
        packets.register(ClientUnmutedS2CPacket.class);
        packets.register(ClientConnectedS2CPacket.class);
        packets.register(ClientDisconnectedS2CPacket.class);

        // source info packets
        packets.register(SourceInfoC2SPacket.class);
        packets.register(SourceInfoS2CPacket.class);

        // audio sync packets
        packets.register(AudioEndPlayerC2SPacket.class);
        packets.register(AudioEndSourceS2CPacket.class);
        packets.register(AudioResetStatsC2SPacket.class);
        packets.register(AudioStatsS2CPacket.class);
    }

    public static byte[] write(Packet packet) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeByte(packets.getType(packet));
        try {
            packet.write(out);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out.toByteArray();
    }

    public static Packet read(ByteArrayDataInput buf) {
        Packet packet = packets.byType(buf.readByte());
        if (packet != null) {
            try {
                packet.read(buf);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return packet;
        }

        return null;
    }
}
