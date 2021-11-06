package su.plo.voice.protocol.packets.tcp;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.Getter;
import su.plo.voice.protocol.packets.Packet;

import java.io.IOException;

@AllArgsConstructor
public class AudioEndPlayerC2SPacket implements Packet<ServerTcpPacketListener> {
    @Getter
    private short distance;
    @Getter
    private long sequenceNumber;

    public AudioEndPlayerC2SPacket() {
    }

    @Override
    public void read(ByteArrayDataInput buf) throws IOException {
        this.distance = buf.readShort();
        this.sequenceNumber = buf.readLong();
    }

    @Override
    public void write(ByteArrayDataOutput buf) throws IOException {
        buf.writeShort(distance);
        buf.writeLong(sequenceNumber);
    }

    @Override
    public void handle(ServerTcpPacketListener listener) {
        listener.handle(this);
    }
}
