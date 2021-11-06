package su.plo.voice.server.mod.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import su.plo.voice.api.player.VoicePlayer;
import su.plo.voice.server.VoiceServer;
import su.plo.voice.server.mod.player.PlayerManagerMod;

public class VoiceReconnect {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("vrc")
                .requires(source ->
                        CommandManager.requiresPermission(source, "voice.reconnect")
                )
                .executes(ctx -> {
                    ctx.getSource().sendSuccess(new TextComponent(
                            VoiceServer.getInstance().getMessagePrefix("reconnect_sent")
                            ), false);
                    ServerPlayer serverPlayer = ctx.getSource().getPlayerOrException();

                    PlayerManagerMod playerManager = (PlayerManagerMod) VoiceServer.getAPI().getPlayerManager();
                    VoicePlayer player = playerManager.getByUniqueId(serverPlayer.getUUID());
                    if (player == null) {
                        player = playerManager.create(serverPlayer);
                    }

                    VoiceServer.getNetwork().reconnectClient(player);
                    return 1;
                }));
    }
}
