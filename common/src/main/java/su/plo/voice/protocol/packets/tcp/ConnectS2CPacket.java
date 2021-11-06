package su.plo.voice.protocol.packets.tcp;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.Getter;
import su.plo.voice.protocol.packets.Packet;

import java.io.IOException;

@AllArgsConstructor
public class ConnectS2CPacket implements Packet<ClientTcpPacketListener> {
    @Getter
    private String token;
    @Getter
    private String ip;
    @Getter
    private int port;

    public ConnectS2CPacket() {
    }

    @Override
    public void read(ByteArrayDataInput buf) throws IOException {
        this.token = buf.readUTF();
        this.ip = buf.readUTF();
        this.port = buf.readInt();
    }

    @Override
    public void write(ByteArrayDataOutput buf) throws IOException {
        buf.writeUTF(token);
        buf.writeUTF(ip);
        buf.writeInt(port);
    }

    @Override
    public void handle(ClientTcpPacketListener listener) {
        listener.handle(this);
    }
}
