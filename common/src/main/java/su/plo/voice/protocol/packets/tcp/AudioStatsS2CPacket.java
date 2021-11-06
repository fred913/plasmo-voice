package su.plo.voice.protocol.packets.tcp;


import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.Getter;
import su.plo.voice.protocol.packets.Packet;

import java.io.IOException;

@AllArgsConstructor
public class AudioStatsS2CPacket implements Packet<ClientTcpPacketListener> {
    @Getter
    private int packetsLost;

    public AudioStatsS2CPacket() {
    }

    @Override
    public void read(ByteArrayDataInput buf) throws IOException {
        this.packetsLost = buf.readInt();
    }

    @Override
    public void write(ByteArrayDataOutput buf) throws IOException {
        buf.write(packetsLost);
    }

    @Override
    public void handle(ClientTcpPacketListener listener) {
        listener.handle(this);
    }
}