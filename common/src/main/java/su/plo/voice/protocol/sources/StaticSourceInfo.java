package su.plo.voice.protocol.sources;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.api.Pos3d;

import java.io.IOException;

public class StaticSourceInfo extends SourceInfo {
    @Getter
    private Pos3d position;
    @Getter
    private int angle;
    @Getter
    private Pos3d direction;

    public StaticSourceInfo() {
    }

    public StaticSourceInfo(int id, Pos3d position, @Nullable Pos3d direction, int angle, boolean visible) {
        super(Type.STATIC, id, visible);
        this.position = position;
        this.direction = direction;
        this.angle = angle;
    }

    @Override
    public void read(ByteArrayDataInput buf) throws IOException {
        super.read(buf);

        this.position = new Pos3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
        if (buf.readBoolean()) {
            this.angle = buf.readInt();
            this.direction = new Pos3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
        }
    }

    @Override
    public void write(ByteArrayDataOutput buf) throws IOException {
        super.write(buf);

        buf.writeDouble(position.getX());
        buf.writeDouble(position.getY());
        buf.writeDouble(position.getZ());

        boolean directional = direction != null && angle > 0 && angle < 360;
        buf.writeBoolean(directional);
        if (directional) {
            buf.writeInt(angle);

            buf.writeDouble(direction.getX());
            buf.writeDouble(direction.getY());
            buf.writeDouble(direction.getZ());
        }
    }
}
