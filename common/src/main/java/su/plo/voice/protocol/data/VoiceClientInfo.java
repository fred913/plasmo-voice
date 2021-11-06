package su.plo.voice.protocol.data;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.IOException;
import java.util.UUID;

@AllArgsConstructor
@Data
public class VoiceClientInfo {
    private int id;
    private UUID uuid;
    private boolean muted;

    public VoiceClientInfo() {
    }

    public void read(ByteArrayDataInput buf) throws IOException {
        this.id = buf.readInt();
        this.uuid = UUID.fromString(buf.readUTF());
        this.muted = buf.readBoolean();
    }

    public void write(ByteArrayDataOutput buf) throws IOException {
        buf.writeInt(id);
        buf.writeUTF(uuid.toString());
        buf.writeBoolean(muted);
    }
}
