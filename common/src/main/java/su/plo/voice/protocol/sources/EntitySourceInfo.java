package su.plo.voice.protocol.sources;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.Getter;

import java.io.IOException;
import java.util.UUID;

public class EntitySourceInfo extends SourceInfo {
    @Getter
    private UUID uuid;

    public EntitySourceInfo() {
    }

    public EntitySourceInfo(int id, UUID uuid) {
        super(Type.ENTITY, id);
        this.uuid = uuid;
    }

    @Override
    public void read(ByteArrayDataInput buf) throws IOException {
        super.read(buf);
        this.uuid = UUID.fromString(buf.readUTF());
    }

    @Override
    public void write(ByteArrayDataOutput buf) throws IOException {
        super.write(buf);
        buf.writeUTF(uuid.toString());
    }
}
