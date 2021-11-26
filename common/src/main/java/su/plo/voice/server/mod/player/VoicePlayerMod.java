package su.plo.voice.server.mod.player;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.api.Pos3d;
import su.plo.voice.api.player.VoicePlayer;
import su.plo.voice.protocol.packets.tcp.PermissionsS2CPacket;
import su.plo.voice.server.VoiceServer;
import su.plo.voice.server.mod.VoiceServerMod;

import java.util.UUID;

public class VoicePlayerMod implements VoicePlayer {
    private final PlayerManagerMod playerManager;
    @Setter
    @Getter
    private ServerPlayer serverPlayer;
    @Setter
    @Getter
    private int id;

    @Setter
    @Getter
    @Nullable
    private String modLoader;

    public VoicePlayerMod(PlayerManagerMod playerManager, int id, ServerPlayer player, @Nullable String modLoader) {
        this.playerManager = playerManager;
        this.serverPlayer = player;
        this.id = id;
        this.modLoader = modLoader;
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
    public boolean inRadius(String world, Pos3d pos, double maxDistanceSquared) {
        if (!getWorld().equals(world)) {
            return false;
        }

        Vec3 position = new Vec3(pos.getX(), pos.getY(), pos.getZ());
        return position.distanceToSqr(serverPlayer.position()) < maxDistanceSquared;
    }

    @Override
    public Pos3d getPosition() {
        Vec3 position = serverPlayer.position();
        return new Pos3d(position.x, position.y, position.z);
    }

    @Override
    public String getWorld() {
        return ((ServerLevelData) serverPlayer.getLevel().getLevelData()).getLevelName();
    }

    @Override
    public boolean isVanillaPlayer() {
        return false;
    }

    @Override
    public boolean hasPermission(String permission) {
        return playerManager.hasPermission(this.getUniqueId(), permission);
    }

    @Override
    public void updatePermissions() {
        VoiceServer.getNetwork().sendTo(
                this,
                new PermissionsS2CPacket(
                        hasPermission("voice.priority"),
                        hasPermission("voice.speak"),
                        hasPermission("voice.activation"),
                        hasPermission("voice.force_sound_occlusion"),
                        !hasPermission("voice.icons"),
                        getForceDistance(),
                        getForcePriorityDistance()
                )
        );
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
