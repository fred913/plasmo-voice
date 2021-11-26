package su.plo.voice.server.network;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import su.plo.voice.api.player.VoicePlayer;
import su.plo.voice.protocol.packets.Packet;
import su.plo.voice.protocol.packets.tcp.MessageTcp;
import su.plo.voice.server.VoiceServer;
import su.plo.voice.server.mod.network.ServerNetworkHandlerMod;

public class ServerNetworkHandlerForge extends ServerNetworkHandlerMod {
    public void handle(ServerPlayer serverPlayer, FriendlyByteBuf buf) {
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

    @Override
    public void handleJoin(ServerPlayer player) {
        super.handleJoin(player);
    }
}
