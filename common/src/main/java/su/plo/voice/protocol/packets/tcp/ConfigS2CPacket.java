package su.plo.voice.protocol.packets.tcp;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.Getter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Этот пакет отправляется только при входе и релоаде
 * PermissionsS2CPacket может обновляться динамически с конкретными параметрами
 * Например:
 * PermissionsS2CPacket packet = new PermissionsS2CPacket();
 * packet.setPriority(false);
 * player.send(packet);
 *
 * Оно изменит только доступность приорити у игрока и ничего больше
 */
public class ConfigS2CPacket extends PermissionsS2CPacket {
    @Getter
    private int sampleRate;
    @Getter
    private final List<Integer> distances;
    @Getter
    private int defaultDistance;
    @Getter
    private int maxPriorityDistance;
    @Getter
    private int fadeDivisor;
    @Getter
    private int priorityFadeDivisor;

    public ConfigS2CPacket(int sampleRate, List<Integer> distances, int defaultDistance, int maxPriorityDistance,
                           int fadeDivisor, int priorityFadeDivisor,
                           Boolean priority, Boolean speak, Boolean activation,
                           Boolean forceSoundOcclusion, Boolean disableIcons,
                           Integer forceDistance, Integer forcePriorityDistance) {
        super(priority, speak, activation, forceSoundOcclusion, disableIcons, forceDistance, forcePriorityDistance);
        this.sampleRate = sampleRate;
        this.distances = distances;
        this.defaultDistance = defaultDistance;
        this.maxPriorityDistance = maxPriorityDistance;
        this.fadeDivisor = fadeDivisor;
        this.priorityFadeDivisor = priorityFadeDivisor;
    }

    public ConfigS2CPacket() {
        this.distances = new ArrayList<>();
    }

    @Override
    public void write(ByteArrayDataOutput buf) throws IOException {
        super.write(buf);

        buf.writeInt(sampleRate);
        buf.writeInt(distances.size());
        for (int distance : distances) {
            buf.writeInt(distance);
        }
        buf.writeInt(defaultDistance);
        buf.writeInt(maxPriorityDistance);
        buf.writeInt(fadeDivisor);
        buf.writeInt(priorityFadeDivisor);
    }

    @Override
    public void read(ByteArrayDataInput buf) throws IOException {
        super.read(buf);

        sampleRate = buf.readInt();
        int length = buf.readInt();
        for (int i = 0; i < length; i++) {
            distances.add(buf.readInt());
        }
        defaultDistance = buf.readInt();
        maxPriorityDistance = buf.readInt();
        fadeDivisor = buf.readInt();
        priorityFadeDivisor = buf.readInt();
    }

    @Override
    public void handle(ClientTcpPacketListener listener) {
        listener.handle(this);
    }
}
