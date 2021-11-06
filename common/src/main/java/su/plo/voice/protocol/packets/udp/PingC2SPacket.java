package su.plo.voice.protocol.packets.udp;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import su.plo.voice.protocol.packets.Packet;

import java.io.IOException;

public class PingC2SPacket extends AbstractPacketUdp implements Packet<ServerUdpPacketListener> {
    public PingC2SPacket() {
    }

    @Override
    public void read(ByteArrayDataInput buf) throws IOException {
    }

    @Override
    public void write(ByteArrayDataOutput buf) throws IOException {
    }

    @Override
    public void handle(ServerUdpPacketListener listener) {
        listener.handle(this);
    }
}