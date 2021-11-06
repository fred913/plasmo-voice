package su.plo.voice.server.mod.player;

import lombok.Getter;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import su.plo.voice.api.player.VoicePlayer;
import su.plo.voice.server.mod.VoiceServerMod;

import java.util.UUID;

public class VoicePlayerMod implements VoicePlayer {
    private final PlayerManagerMod playerManager;
    @Getter
    private final ServerPlayer serverPlayer;
    private final int id;
    private final String type;

    public VoicePlayerMod(PlayerManagerMod playerManager, int id, ServerPlayer player, String type) {
        this.playerManager = playerManager;
        this.serverPlayer = player;
        this.id = id;
        this.type = type;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return serverPlayer.getGameProfile().getName();
    }

    @Override
    public UUID getUniqueId() {
        return serverPlayer.getUUID();
    }

    @Override
    public boolean isOnline() {
        // there is no OfflinePlayer alternative in mods yet, so players only can be online
        return true;
    }

    @Override
    public void sendMessage(String message) {
        serverPlayer.sendMessage(
                new TextComponent(message),
                VoiceServerMod.NIL_UUID
        );
    }

    @Override
    public void sendTranslatableMessage(String message, Object... args) {
        serverPlayer.sendMessage(
                new TranslatableComponent(message, args),
                VoiceServerMod.NIL_UUID
        );
    }

    @Override
    public boolean inRadius(VoicePlayer player, double maxDistanceSquared) {
        ServerPlayer serverPlayer = ((VoicePlayerMod) player).getServerPlayer();
        return this.serverPlayer.position().distanceToSqr(serverPlayer.position()) <= maxDistanceSquared;
    }

    @Override
    public boolean isVanillaPlayer() {
        return false;
    }

    @Override
    public String getModLoader() {
        return type;
    }

    @Override
    public boolean hasPermission(String permission) {
        return playerManager.hasPermission(getUniqueId(), permission);
    }

    @Override
    public int getForceDistance() {
        return 0;
    }

    @Override
    public int getForcePriorityDistance() {
        return 0;
    }
}
