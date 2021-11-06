package su.plo.voice.server.mod.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import su.plo.voice.server.VoiceServer;
import su.plo.voice.server.mod.VoiceServerMod;
import su.plo.voice.server.socket.SocketServerUDP;

import java.util.List;
import java.util.stream.Collectors;

public class VoiceList {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("vlist")
                .requires(source ->
                        CommandManager.requiresPermission(source, "voice.list")
                )
                .executes(ctx -> {
                    List<String> clients = SocketServerUDP.clients.values().stream()
                            .map(client -> client.getPlayer().getName())
                            .collect(Collectors.toList());

                    ctx.getSource().sendSuccess(new TextComponent(
                            VoiceServer.getInstance().getMessagePrefix("list")
                                    .replace("{count}", String.valueOf(clients.size()))
                                    .replace("{online_players}", String.valueOf(VoiceServerMod.getServer().getPlayerCount()))
                                    .replace("{players}", String.join(", ", clients))
                    ), false);
                    return 1;
                }));
    }
}
