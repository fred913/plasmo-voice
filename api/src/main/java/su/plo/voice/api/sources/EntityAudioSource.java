package su.plo.voice.api.sources;

import su.plo.voice.api.entity.VoiceEntity;

import javax.annotation.Nonnull;
import java.util.UUID;

public interface EntityAudioSource extends AudioSource {
    /**
     * ru_RU
     * Получить entity UUID источника
     */
    @Nonnull
    UUID getEntityId();

    /**
     * ru_RU
     * Получить VoiceEntity
     */
    @Nonnull
    VoiceEntity getEntity();
}
