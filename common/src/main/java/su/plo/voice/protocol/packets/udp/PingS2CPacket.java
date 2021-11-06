package su.plo.voice.protocol.packets.udp;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import su.plo.voice.protocol.packets.Packet;

import java.io.IOException;

public class PingS2CPacket extends AbstractPacketUdp implements Packet<ClientUdpPacketListener> {
    public PingS2CPacket() {
    }

    @Override
    public void read(ByteArrayDataInput buf) throws IOException {
    }

    @Override
    public void write(ByteArrayDataOutput buf) throws IOException {
    }

    @Override
    public void handle(ClientUdpPacketListener listener) {
        listener.handle(this);
    }
}