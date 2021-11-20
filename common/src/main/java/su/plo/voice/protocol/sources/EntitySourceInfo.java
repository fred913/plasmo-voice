package su.plo.voice.protocol.sources;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.Getter;

import java.io.IOException;

public class EntitySourceInfo extends SourceInfo {
    @Getter
    private int entityId;

    public EntitySourceInfo() {
    }

    public EntitySourceInfo(int id, int entityId, boolean visible) {
        super(Type.ENTITY, id, visible);
        this.entityId = entityId;
    }

    @Override
    public void read(ByteArrayDataInput buf) throws IOException {
        super.read(buf);
        this.entityId = buf.readInt();
    }

    @Override
    public void write(ByteArrayDataOutput buf) throws IOException {
        super.write(buf);
        buf.writeInt(entityId);
    }
}
