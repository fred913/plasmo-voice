package su.plo.voice.protocol.packets.udp;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.Getter;

import java.io.IOException;

public class AudioDirectC2SPacket extends AudioRawC2SPacket {
    @Getter
    private int sourceId;

    public AudioDirectC2SPacket() {
    }

    public AudioDirectC2SPacket(int sourceId, byte[] data) {
        super(data);
        this.sourceId = sourceId;
    }

    @Override
    public void read(ByteArrayDataInput buf) throws IOException {
        this.sourceId = buf.readShort();

        super.read(buf);
    }

    @Override
    public void write(ByteArrayDataOutput buf) throws IOException {
        buf.writeShort(sourceId);

        super.write(buf);
    }

    @Override
    public void handle(ServerUdpPacketListener listener) {
        listener.handle(this);
    }
}
