package su.plo.voice.server.mod.player;

import lombok.Getter;
import net.minecraft.server.level.ServerPlayer;
import su.plo.voice.api.player.PlayerManager;
import su.plo.voice.api.player.VoicePlayer;
import su.plo.voice.server.VoiceServer;
import su.plo.voice.server.config.Configuration;
import su.plo.voice.server.mod.VoiceServerMod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerManagerMod implements PlayerManager {
    @Getter
    private final Map<UUID, Map<String, Boolean>> permissions = new HashMap<>();
    protected final Map<UUID, VoicePlayer> clients = new HashMap<>();

    public synchronized boolean hasPermission(UUID playerId, String permission) {
        Map<String, Boolean> perms = permissions.get(playerId);
        if (perms != null) {
            return perms.getOrDefault(permission, hasDefaultPermission(playerId, permission));
        } else {
            return hasDefaultPermission(playerId, permission);
        }
    }

    public synchronized void setPermission(VoicePlayer player, String permission, boolean value) {
        if (hasDefaultPermission(player.getUniqueId(), permission) == value) {
            unSetPermission(player, permission);
            return;
        }

        if (permissions.containsKey(player.getUniqueId())) {
            Map<String, Boolean> perms = permissions.get(player.getUniqueId());
            perms.put(permission, value);
        } else {
            Map<String, Boolean> perms = new HashMap<>();
            perms.put(permission, value);
            permissions.put(player.getUniqueId(), perms);
        }

        VoiceServer.getInstance().saveData(true);

        // send permissions update to player
        player.updatePermissions();
    }

    public synchronized void unSetPermission(VoicePlayer player, String permission) {
        if (permissions.containsKey(player.getUniqueId())) {
            Map<String, Boolean> perms = permissions.get(player.getUniqueId());
            perms.remove(permission);
        }

        VoiceServer.getInstance().saveData(true);

        // send permissions update to player
        player.updatePermissions();
    }

    public boolean hasDefaultPermission(UUID playerId, String permission) {
        String defaultPermission = getDefaultPermission(permission);
        if (defaultPermission.equals("op")) {
            return isOp(playerId);
        } else {
            return !defaultPermission.equals("false");
        }
    }

    private String getDefaultPermission(String permission) {
        Configuration section = VoiceServer.getInstance().getConfig().getSection("permissions");
        Object obj = section.getSelf().get(permission);
        if (obj == null) {
            return "";
        }

        return obj instanceof String ? (String) obj : "";
    }

    @Override
    public synchronized VoicePlayer getByUniqueId(UUID playerId) {
        ServerPlayer serverPlayer = VoiceServerMod.getServer().getPlayerList().getPlayer(playerId);
        if (serverPlayer == null) {
            return null;
        }

        VoicePlayer player = clients.get(playerId);
        if (player == null) {
            player = new VoicePlayerMod(this, 0, serverPlayer, null);
            clients.put(playerId, player);
        }

        return player;
    }

    public synchronized VoicePlayer getByServerPlayer(ServerPlayer serverPlayer) {
        VoicePlayerMod player = (VoicePlayerMod) clients.get(serverPlayer.getUUID());
        if (player != null) {
            player.setServerPlayer(serverPlayer);
        } else {
            player = new VoicePlayerMod(this, 0, serverPlayer, null);
            clients.put(serverPlayer.getUUID(), player);
        }

        return player;
    }

    @Override
    public boolean isVanillaPlayer(UUID playerId) {
        return false;
    }

    public static boolean isOp(UUID player) {
        return VoiceServerMod.getServer().getPlayerList().isOp(VoiceServerMod.getServer().getProfileCache().get(player).get());
    }

    public static boolean isOp(ServerPlayer player) {
        return VoiceServerMod.getServer().getPlayerList().isOp(player.getGameProfile());
    }
}
