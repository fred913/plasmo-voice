package su.plo.voice.server.mod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import su.plo.voice.server.VoiceServer;
import su.plo.voice.server.mod.player.PlayerManagerMod;

public class CommandManager {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean permissions) {
        VoiceList.register(dispatcher);
        VoiceReconnect.register(dispatcher);
        VoiceReload.register(dispatcher);

        VoiceMute.register(dispatcher);
        VoiceMuteList.register(dispatcher);
        VoiceUnmute.register(dispatcher);
        if (permissions) {
            VoicePermissions.register(dispatcher);
        }
    }

    public static boolean requiresPermission(CommandSourceStack source, String permission) {
        if (source.getEntity() == null) {
            return true;
        }

        try {
            return ((PlayerManagerMod) VoiceServer.getAPI().getPlayerManager()).hasPermission(source.getPlayerOrException().getUUID(), permission);
        } catch (CommandSyntaxException ignored) {
            return false;
        }
    }
}
