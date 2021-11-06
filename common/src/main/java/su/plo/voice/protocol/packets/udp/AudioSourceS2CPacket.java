package su.plo.voice.protocol.packets.udp;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.Getter;

import java.io.IOException;

public class AudioSourceS2CPacket extends AudioDirectS2CPacket {
    @Getter
    private short distance;

    public AudioSourceS2CPacket() {
    }

    public AudioSourceS2CPacket(int sourceId, short distance, byte[] data) {
        super(sourceId, data);
        this.distance = distance;
    }

    @Override
    public void read(ByteArrayDataInput buf) throws IOException {
        this.distance = buf.readShort();

        super.read(buf);
    }

    @Override
    public void write(ByteArrayDataOutput buf) throws IOException {
        buf.writeShort(distance);

        super.write(buf);
    }

    @Override
    public void handle(ClientUdpPacketListener listener) {
        listener.handle(this);
    }
}
