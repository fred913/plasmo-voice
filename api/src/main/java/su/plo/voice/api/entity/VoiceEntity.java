package su.plo.voice.api.entity;

import su.plo.voice.api.Pos3d;

import java.util.UUID;

public interface VoiceEntity {
    int getEntityId();

    UUID getUniqueId();

    boolean isAlive();

    String getWorld();

    Pos3d getPosition();
}
