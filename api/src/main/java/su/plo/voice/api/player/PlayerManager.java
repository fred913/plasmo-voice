package su.plo.voice.api.player;

import java.util.UUID;

public interface PlayerManager {
    VoicePlayer getByUniqueId(UUID playerId);

    boolean isVanillaPlayer(UUID playerId);

    // online player
    VoicePlayer create(int id, UUID playerId, String type);

    // offline player
    VoicePlayer create(UUID playerId);
}
