package su.plo.voice.protocol.sources;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.IOException;

@AllArgsConstructor
public abstract class SourceInfo {
    @Getter
    protected Type type;
    @Getter
    protected int id;

    public SourceInfo() {
    }

    public static SourceInfo create(ByteArrayDataInput buf) {
        return switch (Type.valueOf(buf.readUTF())) {
            case PLAYER -> new PlayerSourceInfo();
            case STATIC -> new StaticSourceInfo();
            default -> null;
        };
    }

    public void read(ByteArrayDataInput buf) throws IOException {
        this.id = buf.readInt();
    }

    public void write(ByteArrayDataOutput buf) throws IOException {
        buf.writeUTF(type.name());
        buf.writeInt(id);
    }

    public enum Type {
        PLAYER,
        STATIC,
        ENTITY
    }
}
