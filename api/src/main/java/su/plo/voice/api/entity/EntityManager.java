package su.plo.voice.api.entity;

import java.util.UUID;

public interface EntityManager {
    VoiceEntity getByUniqueId(UUID entityId);
}
