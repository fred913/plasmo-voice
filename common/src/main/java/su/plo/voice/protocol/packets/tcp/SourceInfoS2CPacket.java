package su.plo.voice.protocol.packets.tcp;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.Getter;
import su.plo.voice.protocol.packets.Packet;
import su.plo.voice.protocol.sources.SourceInfo;

import java.io.IOException;

@AllArgsConstructor
public class SourceInfoS2CPacket implements Packet<ClientTcpPacketListener> {
    @Getter
    private SourceInfo info;

    public SourceInfoS2CPacket() {
    }

    @Override
    public void read(ByteArrayDataInput buf) throws IOException {
        info = SourceInfo.create(buf);
        info.read(buf);
    }

    @Override
    public void write(ByteArrayDataOutput buf) throws IOException {
        info.write(buf);
    }

    @Override
    public void handle(ClientTcpPacketListener listener) {
        listener.handle(this);
    }
}
