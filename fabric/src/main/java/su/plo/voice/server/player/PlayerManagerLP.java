package su.plo.voice.server.player;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.user.UserDataRecalculateEvent;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.util.Tristate;
import su.plo.voice.api.player.VoicePlayer;
import su.plo.voice.server.VoiceServer;
import su.plo.voice.server.mod.player.PlayerManagerMod;

import java.util.UUID;

public class PlayerManagerLP extends PlayerManagerMod {
    private final UserManager userManager;

    public PlayerManagerLP(LuckPerms luckPerms) {
        this.userManager = luckPerms.getUserManager();

        EventBus eventBus = luckPerms.getEventBus();
        eventBus.subscribe(UserDataRecalculateEvent.class, (e) -> {
            User user = e.getUser();
            VoicePlayer player = VoiceServer.getAPI().getPlayerManager().getByUniqueId(user.getUniqueId());
            if (player != null) {
                player.updatePermissions();
            }
        });
    }

    @Override
    public synchronized boolean hasPermission(UUID playerId, String permission) {
        User user = userManager.getUser(playerId);
        if (user == null) {
            throw new IllegalStateException("Player is offline");
        }

        Tristate check = user.getCachedData().getPermissionData().checkPermission(permission);
        if (check == Tristate.UNDEFINED) {
            return super.hasDefaultPermission(playerId, permission);
        }

        return check.asBoolean();
    }
}
