package su.plo.voice.api.player;

import java.util.UUID;

public interface PlayerManager {
    VoicePlayer getByUniqueId(UUID playerId);

    boolean isVanillaPlayer(UUID playerId);
}
