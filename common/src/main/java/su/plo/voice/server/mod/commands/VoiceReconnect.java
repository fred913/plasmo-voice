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

                    VoicePlayer player = ((PlayerManagerMod) VoiceServer.getAPI().getPlayerManager())
                            .getByServerPlayer(serverPlayer);

                    VoiceServer.getNetwork().reconnectClient(player);
                    return 1;
                }));
    }
}
