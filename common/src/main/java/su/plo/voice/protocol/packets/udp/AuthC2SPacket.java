package su.plo.voice.protocol.packets.udp;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.Getter;
import su.plo.voice.protocol.packets.Packet;

import java.io.IOException;

@AllArgsConstructor
public class AuthC2SPacket extends AbstractPacketUdp implements Packet<ServerUdpPacketListener> {
    @Getter
    private String token;

    public AuthC2SPacket() {
    }

    @Override
    public void read(ByteArrayDataInput buf) throws IOException {
        this.token = buf.readUTF();
    }

    @Override
    public void write(ByteArrayDataOutput buf) throws IOException {
        buf.writeUTF(token);;
    }

    @Override
    public void handle(ServerUdpPacketListener listener) {
//        listener.handle(this);
    }
}
