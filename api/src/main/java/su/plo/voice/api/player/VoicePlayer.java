package su.plo.voice.api.player;

import java.util.UUID;

public interface VoicePlayer {
    int getId();

    String getName();

    UUID getUniqueId();

    boolean isOnline();

    void sendMessage(String message);

    void sendTranslatableMessage(String message, Object ...args);

    boolean inRadius(VoicePlayer player, double maxDistanceSquared);

    boolean isVanillaPlayer();

    String getModLoader();

    boolean hasPermission(String permission);

    int getForceDistance();

    int getForcePriorityDistance();
}
