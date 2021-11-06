package su.plo.voice.protocol.sources;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.Getter;
import su.plo.voice.protocol.data.VoiceClientInfo;

import java.io.IOException;

public class PlayerSourceInfo extends SourceInfo {
    @Getter
    private final VoiceClientInfo client;

    public PlayerSourceInfo() {
        client = new VoiceClientInfo();
    }

    public PlayerSourceInfo(int id, VoiceClientInfo client) {
        super(Type.PLAYER, id);
        this.client = client;
    }

    @Override
    public void read(ByteArrayDataInput buf) throws IOException {
        super.read(buf);
        client.read(buf);
    }

    @Override
    public void write(ByteArrayDataOutput buf) throws IOException {
        super.write(buf);
        client.write(buf);
    }
}
