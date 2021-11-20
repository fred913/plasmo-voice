package su.plo.voice.server;

import su.plo.voice.api.Pos3d;
import su.plo.voice.api.player.VoicePlayer;
import su.plo.voice.api.sources.*;
import su.plo.voice.protocol.data.VoiceClientInfo;
import su.plo.voice.protocol.sources.EntitySourceInfo;
import su.plo.voice.protocol.sources.PlayerSourceInfo;
import su.plo.voice.protocol.sources.SourceInfo;
import su.plo.voice.protocol.sources.StaticSourceInfo;
import su.plo.voice.server.sources.DirectAudioSourceImpl;
import su.plo.voice.server.sources.EntityAudioSourceImpl;
import su.plo.voice.server.sources.PlayerAudioSourceImpl;
import su.plo.voice.server.sources.StaticAudioSourceImpl;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class SourceManagerImpl implements SourceManager {
    private final AtomicInteger ids = new AtomicInteger(1);
    private final Map<Integer, AudioSource> sources = new HashMap<>();

    public SourceInfo getInfo(int sourceId) {
        AudioSource source = sources.get(sourceId);
        if (source != null) {
            if (source instanceof PlayerAudioSource) {
                VoicePlayer player = ((PlayerAudioSource) source).getPlayer();
                return new PlayerSourceInfo(
                        sourceId,
                        new VoiceClientInfo(
                                sourceId,
                                player.getUniqueId(),
                                VoiceServer.getAPI().isMuted(player.getUniqueId())
                        ),
                        source.isVisible()
                );
            } else if (source instanceof StaticAudioSource) {
                StaticAudioSource staticSource = (StaticAudioSource) source;
                return new StaticSourceInfo(
                        sourceId,
                        staticSource.getPosition(),
                        staticSource.getDirection(),
                        staticSource.getAngle(),
                        source.isVisible()
                );
            } else if (source instanceof EntityAudioSource) {
                EntityAudioSource entitySource = (EntityAudioSource) source;
                return new EntitySourceInfo(
                        sourceId,
                        entitySource.getEntity().getEntityId(),
                        source.isVisible()
                );
            }
        }

        return null;
    }

    public synchronized void close() {
        sources.values().forEach(AudioSource::close);
    }

    @Override
    public synchronized AudioSource byId(int sourceId) {
        return sources.get(sourceId);
    }

    @Override
    public synchronized void destroy(int sourceId) {
        AudioSource source = sources.get(sourceId);
        if (source != null) {
            source.sendEnd((short) 64, 0L);
        }
    }

    @Override
    public synchronized PlayerAudioSource registerPlayer(@Nonnull UUID playerId) {
        VoicePlayer player = VoiceServer.getAPI().getPlayerManager().getByUniqueId(playerId);
        if (player != null && player.getId() > 0) {
            return (PlayerAudioSource) sources.get(player.getId());
        }

        PlayerAudioSource source = new PlayerAudioSourceImpl(ids.getAndIncrement(), playerId);
        sources.put(source.getId(), source);

        return source;
    }

    @Override
    public synchronized StaticAudioSource registerStatic(@Nonnull String worldName, @Nonnull Pos3d position) {
        StaticAudioSource source = new StaticAudioSourceImpl(ids.getAndIncrement(), worldName, position);
        sources.put(source.getId(), source);

        return source;
    }

    @Override
    public synchronized StaticAudioSource registerStatic(@Nonnull String worldName, @Nonnull Pos3d position, @Nonnull AudioSource.Application mode, int bitrate) {
        StaticAudioSource source = new StaticAudioSourceImpl(ids.getAndIncrement(), worldName, position, mode, bitrate);
        sources.put(source.getId(), source);

        return source;
    }

    @Override
    public synchronized EntityAudioSource registerEntity(@Nonnull UUID entityId) {
        EntityAudioSource source = new EntityAudioSourceImpl(ids.getAndIncrement(), entityId);
        sources.put(source.getId(), source);

        return source;
    }

    @Override
    public synchronized EntityAudioSource registerEntity(@Nonnull UUID entityId, @Nonnull AudioSource.Application mode, int bitrate) {
        EntityAudioSource source = new EntityAudioSourceImpl(ids.getAndIncrement(), entityId, mode, bitrate);
        sources.put(source.getId(), source);

        return source;
    }

    @Override
    public synchronized DirectAudioSource registerDirect(@Nonnull UUID playerId) {
        DirectAudioSource source = new DirectAudioSourceImpl(ids.getAndIncrement(), playerId);
        sources.put(source.getId(), source);

        return source;
    }

    @Override
    public synchronized DirectAudioSource registerDirect(@Nonnull UUID playerId, @Nonnull AudioSource.Application mode, int bitrate) {
        DirectAudioSource source = new DirectAudioSourceImpl(ids.getAndIncrement(), playerId, mode, bitrate);
        sources.put(source.getId(), source);

        return source;
    }
}