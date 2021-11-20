package su.plo.voice.api;


import su.plo.voice.api.entity.EntityManager;
import su.plo.voice.api.event.EventBus;
import su.plo.voice.api.player.PlayerManager;
import su.plo.voice.api.sources.SourceManager;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface PlasmoVoiceAPI {
    /**
     * Mute the player
     *
     * @param player       Player UUID
     * @param duration     Duration of the mute in durationTime
     * @param durationUnit DurationUnit, can be null, if duration is 0
     * @param reason       Reason for the mute
     * @param silent       If true, the player won't see the message about the mute
     */
    void mute(UUID player, long duration, @Nullable DurationUnit durationUnit, @Nullable String reason, boolean silent);

    /**
     * Unmute the player
     *
     * @param player Player UUID
     * @param silent if true, player won't see the message that they are no longer muted
     * @return returns true, if the player is no longer muted. false, if the player wasn't muted, to begin with
     */
    boolean unmute(UUID player, boolean silent);

    /**
     * Check if the player is muted
     *
     * @param player Player UUID
     * @return true if true, false if false 5Head
     */
    boolean isMuted(UUID player);

    /**
     * Map of the muted players
     *
     * @return Map (key - Player UUID, value - ServerMutedEntity)
     */
    Map<UUID, ServerMuteEntry> getMutedMap();

    /**
     * @param player Online player UUID
     * @return true, if the player has the mod installed
     */
    boolean hasVoiceChat(UUID player);

    /**
     * Returns ModLoader of the player
     *
     * @param player UUID игрока онлайн
     * @return fabric/forge or null, if the player doesn't have the mod installed
     */
    @Nullable
    String getPlayerModLoader(UUID player);

    /**
     * @return List of UUIDs of the players with the mod installed
     */
    List<UUID> getPlayers();

    /**
     * @return Count of players with the mod installed
     */
    int getPlayerCount();

    /**
     * @return Event bus for registering event listeners
     */
    EventBus getEventBus();

    /**
     * @return  ...
     */
    EntityManager getEntityManager();

    /**
     * @return ...
     */
    PlayerManager getPlayerManager();

    /**
     * Source manager для создания источников звука
     */
    SourceManager getSourceManager();
}
