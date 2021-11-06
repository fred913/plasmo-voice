package su.plo.voice.protocol.packets.udp;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.Getter;

import java.io.IOException;

public class AudioPlayerC2SPacket extends AudioRawC2SPacket {
    @Getter
    private short distance;

    public AudioPlayerC2SPacket() {
    }

    public AudioPlayerC2SPacket(short distance, byte[] data) {
        super(data);
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
    public void handle(ServerUdpPacketListener listener) {
        listener.handle(this);
    }
}
