package su.plo.voice.protocol.packets.tcp;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.Getter;
import su.plo.voice.protocol.data.VoiceClientInfo;
import su.plo.voice.protocol.packets.Packet;

import java.io.IOException;
import java.util.UUID;

@AllArgsConstructor
public class ClientConnectedS2CPacket implements Packet<ClientTcpPacketListener> {
    @Getter
    private VoiceClientInfo client;

    public ClientConnectedS2CPacket() {}

    @Override
    public void write(ByteArrayDataOutput buf) throws IOException {
        buf.writeInt(client.getId());
        buf.writeUTF(client.getUuid().toString());
        buf.writeBoolean(client.isMuted());
    }

    @Override
    public void read(ByteArrayDataInput buf) throws IOException {
        client = new VoiceClientInfo(
                buf.readInt(),
                UUID.fromString(buf.readUTF()),
                buf.readBoolean()
        );
    }

    @Override
    public void handle(ClientTcpPacketListener listener) {
        listener.handle(this);
    }
}
