package su.plo.voice.protocol.packets;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class PacketHelper {
    private final AtomicInteger lastRegistered = new AtomicInteger(0);
    private final Map<Integer, Class<? extends Packet>> packets = new HashMap<>();

    public void register(Class<? extends Packet> clazz) {
        packets.put(lastRegistered.getAndIncrement(), clazz);
    }

    public Packet byType(int type) {
        Class<? extends Packet> clazz = packets.get(type);
        if (clazz != null) {
            try {
                return clazz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public int getType(Packet packet) {
        return packets
                .entrySet()
                .stream()
                .filter(entry -> packet.getClass().equals(entry.getValue()))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(-1);
    }
}
