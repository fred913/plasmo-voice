package su.plo.voice.server.mod.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import su.plo.voice.protocol.packets.tcp.ConfigS2CPacket;
import su.plo.voice.server.VoiceServer;
import su.plo.voice.server.config.ServerConfig;

import java.util.ArrayList;

public class VoiceReload {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("vreload")
                .requires(source ->
                        CommandManager.requiresPermission(source, "voice.reload")
                )
                .executes(ctx -> {
                    VoiceServer.getInstance().loadConfig();
                    VoiceServer.getInstance().updateConfig();

                    ServerConfig config = VoiceServer.getServerConfig();

                    VoiceServer.getNetwork().sendToClients(
                            new ConfigS2CPacket(
                                    config.getSampleRate(),
                                    new ArrayList<>(config.getDistances()),
                                    config.getDefaultDistance(),
                                    config.getMaxPriorityDistance(),
                                    config.getFadeDivisor(),
                                    config.getPriorityFadeDivisor(),

                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null
                            ),
                            null
                    );

                    ctx.getSource().sendSuccess(
                            new TextComponent(VoiceServer.getInstance().getMessagePrefix("reloaded")),
                            false
                    );

                    return 1;
                })
        );
    }
}
