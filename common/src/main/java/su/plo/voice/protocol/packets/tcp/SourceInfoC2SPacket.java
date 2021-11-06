package su.plo.voice.protocol.packets.tcp;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.Getter;
import su.plo.voice.protocol.packets.Packet;

import java.io.IOException;

@AllArgsConstructor
public class SourceInfoC2SPacket implements Packet<ServerTcpPacketListener> {
    @Getter
    private int id;

    public SourceInfoC2SPacket() {
    }

    @Override
    public void read(ByteArrayDataInput buf) throws IOException {
        this.id = buf.readInt();
    }

    @Override
    public void write(ByteArrayDataOutput buf) throws IOException {
        buf.writeInt(id);
    }

    @Override
    public void handle(ServerTcpPacketListener listener) {
        listener.handle(this);
    }
}
