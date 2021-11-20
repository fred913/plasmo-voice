package su.plo.voice.api.sources;

import su.plo.voice.api.Pos3d;

import javax.annotation.Nonnull;
import java.util.UUID;

public interface SourceManager {
    /**
     * ru_RU
     * Получить источник по id
     */
    AudioSource byId(int sourceId);

    /**
     * Destroy audio source
     */
    void destroy(int sourceId);

    /**
     * Register player audio source
     */
    PlayerAudioSource registerPlayer(@Nonnull UUID playerId);

    /**
     * Register static audio source
     */
    StaticAudioSource registerStatic(@Nonnull String worldName, @Nonnull Pos3d position);

    /**
     * Register static audio source with encoder settings
     */
    StaticAudioSource registerStatic(@Nonnull String worldName, @Nonnull Pos3d position, @Nonnull AudioSource.Application mode, int bitrate);

    /**
     * Register entity audio source
     */
    EntityAudioSource registerEntity(@Nonnull UUID entityId);

    /**
     * Register entity audio source with encoder settings
     */
    EntityAudioSource registerEntity(@Nonnull UUID entityId, @Nonnull AudioSource.Application mode, int bitrate);

    /**
     * Register direct audio source
     */
    DirectAudioSource registerDirect(@Nonnull UUID playerId);

    /**
     * Register direct audio source with encoder settings
     */
    DirectAudioSource registerDirect(@Nonnull UUID playerId, @Nonnull AudioSource.Application mode, int bitrate);
}
