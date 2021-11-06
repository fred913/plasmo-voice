package su.plo.voice.server.mod.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.players.PlayerList;
import su.plo.voice.server.VoiceServer;

public class VoiceUnmute {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("vunmute")
                .requires(source ->
                        CommandManager.requiresPermission(source, "voice.unmute")
                )
                .then(Commands.argument("targets", GameProfileArgument.gameProfile()).suggests((commandContext, suggestionsBuilder) -> {
                            PlayerList playerList = commandContext.getSource().getServer().getPlayerList();
                            return SharedSuggestionProvider.suggest(playerList.getPlayers().stream()
                                    .map((serverPlayer) -> serverPlayer.getGameProfile().getName()), suggestionsBuilder);
                        })
                        .executes(ctx -> {
                            GameProfileArgument.getGameProfiles(ctx, "targets").forEach(gameProfile -> {
                                unmute(ctx, gameProfile);
                            });

                            return 1;
                        })));
    }

    private static void unmute(CommandContext<CommandSourceStack> ctx, GameProfile profile) {
        VoiceServer.getInstance().runAsync(() -> {
            if (!VoiceServer.getAPI().unmute(profile.getId(), false)) {
                ctx.getSource().sendFailure(
                        new TextComponent(String.format(VoiceServer.getInstance().getMessagePrefix("not_muted"), profile.getName()))
                );
                return;
            }

            ctx.getSource().sendSuccess(
                    new TextComponent(String.format(VoiceServer.getInstance().getMessagePrefix("unmuted"), profile.getName())),
                    false
            );
        });
    }
}
