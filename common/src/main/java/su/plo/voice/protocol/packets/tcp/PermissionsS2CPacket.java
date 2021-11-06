package su.plo.voice.protocol.packets.tcp;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.Data;
import su.plo.voice.protocol.packets.Packet;

import java.io.IOException;

@Data
@AllArgsConstructor
public class PermissionsS2CPacket implements Packet<ClientTcpPacketListener> {
    private Boolean priority;
    private Boolean speak;
    private Boolean activation;
    private Boolean forceSoundOcclusion;
    private Boolean disableIcons;
    private Integer forceDistance;
    private Integer forcePriorityDistance;

    public PermissionsS2CPacket() {
    }

    @Override
    public void read(ByteArrayDataInput buf) throws IOException {
        if (buf.readBoolean()) {
            this.priority = buf.readBoolean();
        }
        if (buf.readBoolean()) {
            this.speak = buf.readBoolean();
        }
        if (buf.readBoolean()) {
            this.activation = buf.readBoolean();
        }
        if (buf.readBoolean()) {
            this.forceSoundOcclusion = buf.readBoolean();
        }
        if (buf.readBoolean()) {
            this.disableIcons = buf.readBoolean();
        }
        if (buf.readBoolean()) {
            this.forceDistance = buf.readInt();
        }
        if (buf.readBoolean()) {
            this.forcePriorityDistance = buf.readInt();
        }
    }

    @Override
    public void write(ByteArrayDataOutput buf) throws IOException {
        if (writeNullable(buf, priority)) {
            buf.writeBoolean(priority);
        }
        if (writeNullable(buf, speak)) {
            buf.writeBoolean(speak);
        }
        if (writeNullable(buf, activation)) {
            buf.writeBoolean(activation);
        }
        if (writeNullable(buf, forceSoundOcclusion)) {
            buf.writeBoolean(forceSoundOcclusion);
        }
        if (writeNullable(buf, disableIcons)) {
            buf.writeBoolean(disableIcons);
        }
        if (writeNullable(buf, forceDistance)) {
            buf.writeInt(forceDistance);
        }
        if (writeNullable(buf, forcePriorityDistance)) {
            buf.writeInt(forcePriorityDistance);
        }
    }

    private boolean writeNullable(ByteArrayDataOutput buf, Object obj) {
        boolean isNotNull = obj != null;
        buf.writeBoolean(isNotNull);
        return isNotNull;
    }

    @Override
    public void handle(ClientTcpPacketListener listener) {
        listener.handle(this);
    }
}
