package su.plo.voice.server;

import su.plo.voice.protocol.data.VoiceClientInfo;
import su.plo.voice.protocol.sources.PlayerSourceInfo;
import su.plo.voice.protocol.sources.SourceInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class SourceManager {
    private final AtomicInteger ids = new AtomicInteger(1);
    private final Map<Integer, SourceInfo> sources = new HashMap<>();

    public synchronized SourceInfo getInfo(int sourceId) {
        SourceInfo info = sources.get(sourceId);
        if (info != null) {
            if (info instanceof PlayerSourceInfo) {
                UUID playerId = ((PlayerSourceInfo) info).getClient().getUuid();
                info = new PlayerSourceInfo(
                        sourceId,
                        new VoiceClientInfo(sourceId, playerId, VoiceServer.getAPI().isMuted(playerId))
                );
            }
        }

        return info;
    }

    public synchronized SourceInfo registerPlayerSource(UUID playerId) {
        int id = ids.getAndIncrement();
        SourceInfo info = new PlayerSourceInfo(id, new VoiceClientInfo(id, playerId, false));
        sources.put(id, info);
        return info;
    }

    public synchronized void unregister(int sourceId) {
        sources.remove(sourceId);
    }
}
