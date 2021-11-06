package su.plo.voice.protocol.sources;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.Getter;

import java.io.IOException;

public class StaticSourceInfo extends SourceInfo {
    @Getter
    private double x;
    @Getter
    private double y;
    @Getter
    private double z;
    @Getter
    private boolean directional;
    @Getter
    private int angle;

    public StaticSourceInfo() {
    }

    public StaticSourceInfo(int id,
                            double x, double y, double z, boolean directional, int angle) {
        super(Type.STATIC, id);
        this.x = x;
        this.y = y;
        this.z = z;
        this.directional = directional;
        this.angle = angle;
    }

    @Override
    public void read(ByteArrayDataInput buf) throws IOException {
        super.read(buf);

        this.x = buf.readDouble();
        this.y = buf.readDouble();
        this.z = buf.readDouble();
        this.directional = buf.readBoolean();
        if (directional) {
            this.angle = buf.readInt();
        }
    }

    @Override
    public void write(ByteArrayDataOutput buf) throws IOException {
        super.write(buf);

        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeBoolean(directional);
        if (directional) {
            buf.writeInt(angle);
        }
    }
}
