package su.plo.voice.protocol.packets.tcp;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.Getter;
import su.plo.voice.protocol.packets.Packet;

import java.io.IOException;

@AllArgsConstructor
public class AudioEndSourceS2CPacket implements Packet<ClientTcpPacketListener> {
    @Getter
    private int sourceId;
    @Getter
    private long sequenceNumber;

    public AudioEndSourceS2CPacket() {
    }

    @Override
    public void read(ByteArrayDataInput buf) throws IOException {
        this.sourceId = buf.readShort();
        this.sequenceNumber = buf.readLong();
    }

    @Override
    public void write(ByteArrayDataOutput buf) throws IOException {
        buf.writeShort(sourceId);
        buf.writeLong(sequenceNumber);
    }

    @Override
    public void handle(ClientTcpPacketListener listener) {
        listener.handle(this);
    }
}
