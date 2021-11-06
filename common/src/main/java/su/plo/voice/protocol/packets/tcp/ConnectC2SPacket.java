package su.plo.voice.protocol.packets.tcp;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.Getter;
import su.plo.voice.protocol.packets.Packet;

import java.io.IOException;

@AllArgsConstructor
public class ConnectC2SPacket implements Packet<ServerTcpPacketListener> {
    @Getter
    private String token;
    @Getter
    private String version;

    public ConnectC2SPacket() {}

    @Override
    public void read(ByteArrayDataInput buf) throws IOException {
        this.token = buf.readUTF();
        this.version = buf.readUTF();
    }

    @Override
    public void write(ByteArrayDataOutput buf) throws IOException {
        buf.writeUTF(token);
        buf.writeUTF(version);
    }

    @Override
    public void handle(ServerTcpPacketListener listener) {
        listener.handle(this);
    }
}
