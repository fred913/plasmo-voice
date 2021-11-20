package su.plo.voice.api.player;

import su.plo.voice.api.Pos3d;

import java.util.UUID;

public interface VoicePlayer {
    void setId(int id);

    int getId();

    String getName();

    UUID getUniqueId();

    boolean isOnline();

    void sendMessage(String message);

    void sendTranslatableMessage(String message, Object ...args);

    boolean inRadius(String world, Pos3d pos, double maxDistanceSquared);

    String getWorld();

    Pos3d getPosition();

    boolean isVanillaPlayer();

    void setModLoader(String modLoader);

    String getModLoader();

    boolean hasPermission(String permission);

    int getForceDistance();

    int getForcePriorityDistance();
}
