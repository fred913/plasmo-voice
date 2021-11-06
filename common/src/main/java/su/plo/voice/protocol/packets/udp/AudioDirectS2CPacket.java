package su.plo.voice.protocol.packets.udp;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.Getter;

import java.io.IOException;

public class AudioDirectS2CPacket extends AudioRawS2CPacket {
    @Getter
    private int sourceId;

    public AudioDirectS2CPacket() {
    }

    public AudioDirectS2CPacket(int sourceId, byte[] data) {
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
    public void handle(ClientUdpPacketListener listener) {
        listener.handle(this);
    }
}
