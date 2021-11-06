package su.plo.voice.protocol.packets.tcp;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.Getter;
import su.plo.voice.protocol.data.VoiceClientInfo;
import su.plo.voice.protocol.packets.Packet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class ClientsListS2CPacket implements Packet<ClientTcpPacketListener> {
    @Getter
    private final List<VoiceClientInfo> clients;

    public ClientsListS2CPacket() {
        clients = new ArrayList<>();
    }

    @Override
    public void write(ByteArrayDataOutput buf) throws IOException {
        buf.writeInt(clients.size());
        for(VoiceClientInfo client : clients) {
            buf.writeInt(client.getId());
            buf.writeUTF(client.getUuid().toString());
            buf.writeBoolean(client.isMuted());
        }
    }

    @Override
    public void read(ByteArrayDataInput buf) throws IOException {
        clients.clear();
        for(int i = 0; i < buf.readInt(); i++) {
            clients.add(new VoiceClientInfo(
                    buf.readInt(),
                    UUID.fromString(buf.readUTF()),
                    buf.readBoolean()
            ));
        }
    }

    @Override
    public void handle(ClientTcpPacketListener listener) {
        listener.handle(this);
    }
}
