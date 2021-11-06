package su.plo.voice.protocol.packets.udp;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.Getter;
import su.plo.voice.protocol.packets.Packet;

import java.io.IOException;

@AllArgsConstructor
public class AudioRawC2SPacket extends AbstractPacketUdp implements Packet<ServerUdpPacketListener> {
    @Getter
    protected byte[] data;

    public AudioRawC2SPacket() {
    }

    @Override
    public void read(ByteArrayDataInput buf) throws IOException {
        int length = buf.readInt();
        byte[] data = new byte[length];
        buf.readFully(data);
        this.data = data;
    }

    @Override
    public void write(ByteArrayDataOutput buf) throws IOException {
        buf.writeInt(data.length);
        buf.write(data);
    }

    @Override
    public void handle(ServerUdpPacketListener listener) {
        listener.handle(this);
    }
}
