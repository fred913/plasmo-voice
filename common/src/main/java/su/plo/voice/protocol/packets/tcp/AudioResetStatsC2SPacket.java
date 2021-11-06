package su.plo.voice.protocol.packets.tcp;


import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import su.plo.voice.protocol.packets.Packet;

import java.io.IOException;

public class AudioResetStatsC2SPacket implements Packet<ServerTcpPacketListener> {
    public AudioResetStatsC2SPacket() {
    }

    @Override
    public void read(ByteArrayDataInput buf) throws IOException {
    }

    @Override
    public void write(ByteArrayDataOutput buf) throws IOException {
    }

    @Override
    public void handle(ServerTcpPacketListener listener) {
        listener.handle(this);
    }
}