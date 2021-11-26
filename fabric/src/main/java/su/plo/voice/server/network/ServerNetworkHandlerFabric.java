package su.plo.voice.server.network;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import su.plo.voice.api.player.VoicePlayer;
import su.plo.voice.protocol.packets.Packet;
import su.plo.voice.protocol.packets.tcp.MessageTcp;
import su.plo.voice.server.VoiceServer;
import su.plo.voice.server.config.ServerConfigFabric;
import su.plo.voice.server.mod.VoiceServerMod;
import su.plo.voice.server.mod.network.ServerNetworkHandlerMod;
import su.plo.voice.server.mod.player.PlayerManagerMod;
import su.plo.voice.server.socket.SocketServerUDP;

import java.util.List;

public class ServerNetworkHandlerFabric extends ServerNetworkHandlerMod {
    @Override
    public void handleRegisterChannels(List<ResourceLocation> channels, ServerPlayer serverPlayer) {
        if (channels.size() > 0) {
            if (!playerToken.containsKey(serverPlayer.getUUID()) && channels.contains(VoiceServerMod.PLASMO_VOICE)
                    && !SocketServerUDP.clients.containsKey(serverPlayer.getUUID())) {
                VoicePlayer player = ((PlayerManagerMod) VoiceServer.getAPI().getPlayerManager())
                        .getByServerPlayer(serverPlayer);
                VoiceServer.getNetwork().reconnectClient(player);

                if (!channels.contains(FML_HANDSHAKE)) {
                    fabricPlayers.add(player.getUniqueId());
                }
            } else {
                if (((ServerConfigFabric) VoiceServer.getServerConfig()).isClientModRequired() &&
                        !playerToken.containsKey(serverPlayer.getUUID())
                        && !channels.contains(VoiceServerMod.PLASMO_VOICE)) {
                    if (VoiceServer.isLogsEnabled()) {
                        VoiceServer.LOGGER.info("Player {} does not have the mod installed!", serverPlayer.getGameProfile().getName());
                    }

                    VoiceServerMod.getServer().execute(() ->
                            serverPlayer.connection.disconnect(new TextComponent(VoiceServer.getInstance().getMessage("mod_missing_kick_message")))
                    );
                }
            }
        }
    }

    public void handle(MinecraftServer server, ServerPlayer serverPlayer, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender) {
        VoicePlayer player = VoiceServer.getAPI().getPlayerManager()
                .getByUniqueId(serverPlayer.getUUID());

        byte[] data = new byte[buf.readableBytes()];
        buf.duplicate().readBytes(data);
        ByteArrayDataInput in = ByteStreams.newDataInput(data);

        Packet pkt = MessageTcp.read(in);
        if (pkt != null) {
            pkt.handle(getOrCreateListener(player));
        }
    }
}
