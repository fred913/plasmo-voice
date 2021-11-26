package su.plo.voice.server.mod.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import su.plo.voice.api.player.VoicePlayer;
import su.plo.voice.protocol.packets.Packet;
import su.plo.voice.protocol.packets.tcp.ClientTcpPacketListener;
import su.plo.voice.protocol.packets.tcp.ClientUnmutedS2CPacket;
import su.plo.voice.protocol.packets.tcp.MessageTcp;
import su.plo.voice.server.VoiceServer;
import su.plo.voice.server.mod.VoiceServerMod;
import su.plo.voice.server.mod.player.PlayerManagerMod;
import su.plo.voice.server.mod.player.VoicePlayerMod;
import su.plo.voice.server.network.ServerNetworkHandler;
import su.plo.voice.server.socket.SocketClientUDP;
import su.plo.voice.server.socket.SocketServerUDP;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class ServerNetworkHandlerMod extends ServerNetworkHandler {
    protected static final ResourceLocation FML_HANDSHAKE = new ResourceLocation("fml:handshake");
    protected final Set<UUID> fabricPlayers = new HashSet<>();
    protected ScheduledExecutorService scheduler;
    private static ExecutorService executor;

    public ServerNetworkHandlerMod() {
    }

    public boolean isFabricPlayer(ServerPlayer player) {
        return !fabricPlayers.contains(player.getUUID());
    }

    public void start() {
        this.scheduler = Executors.newScheduledThreadPool(1);
        executor = Executors.newSingleThreadExecutor();

        scheduler.scheduleAtFixedRate(() -> {
            VoiceServer.getAPI().getMutedMap().forEach((uuid, muted) -> {
                if (muted.getTo() > 0 && muted.getTo() < System.currentTimeMillis()) {
                    VoiceServer.getAPI().unmute(uuid, false);

                    VoicePlayer player = VoiceServer.getAPI().getPlayerManager().getByUniqueId(uuid);
                    if (player != null && player.isOnline()) {
                        sendToClients(new ClientUnmutedS2CPacket(uuid), null);
                    }
                }
            });
        }, 0L, 5L, TimeUnit.SECONDS);
    }

    public void close() {
        scheduler.shutdown();
        executor.shutdown();
    }

    public void handleRegisterChannels(List<ResourceLocation> channels, ServerPlayer serverPlayer) {
        if (!playerToken.containsKey(serverPlayer.getUUID()) && channels.contains(VoiceServerMod.PLASMO_VOICE)
                && !SocketServerUDP.clients.containsKey(serverPlayer.getUUID())) {
            VoicePlayer player = ((PlayerManagerMod) VoiceServer.getAPI().getPlayerManager())
                    .getByServerPlayer(serverPlayer);

            VoiceServer.getNetwork().reconnectClient(player);

            if (!channels.contains(FML_HANDSHAKE)) {
                fabricPlayers.add(player.getUniqueId());
            }
        }
    }

    public void handleJoin(ServerPlayer player) {
        if (PlayerManagerMod.isOp(player)
                && !SocketServerUDP.started) {
            player.sendMessage(new TextComponent(VoiceServer.getInstance().getPrefix() +
                            String.format("Voice chat is installed but doesn't work. Check if port %d UDP is open.",
                                    VoiceServer.getServerConfig().getPort())),
                    VoiceServerMod.NIL_UUID);
        }
    }

    public void handleQuit(ServerPlayer player) {
        fabricPlayers.remove(player.getUUID());
        playerToken.remove(player.getUUID());
        listeners.remove(player.getUUID());

        SocketClientUDP client = SocketServerUDP.clients.get(player.getUUID());
        if (client != null) {
            client.close();
        }
    }

    @Override
    public void sendTo(VoicePlayer player, Packet<ClientTcpPacketListener> packet) {
        this.sendTo(
                player,
                MessageTcp.write(packet)
        );
    }

    @Override
    public void sendTo(VoicePlayer player, byte[] bytes) {
        ((VoicePlayerMod) player).getServerPlayer().connection.send(
                new ClientboundCustomPayloadPacket(
                        VoiceServerMod.PLASMO_VOICE,
                        new FriendlyByteBuf(Unpooled.wrappedBuffer(bytes))
                )
        );
    }

    @Override
    public void sendToClients(Packet<ClientTcpPacketListener> packet, VoicePlayer except) {
        byte[] bytes = MessageTcp.write(packet);

        for (Map.Entry<UUID, SocketClientUDP> entry : SocketServerUDP.clients.entrySet()) {
            UUID playerId = entry.getKey();
            if (except == null || !except.getUniqueId().equals(playerId)) {
                sendTo(
                        VoiceServer.getAPI().getPlayerManager().getByUniqueId(playerId),
                        bytes
                );
            }
        }
    }

    @Override
    public void sendToNearbyClients(Packet<ClientTcpPacketListener> packet, VoicePlayer player, double maxDistance) {
        double maxDistanceSquared = maxDistance * maxDistance * 1.25F;

        byte[] bytes = MessageTcp.write(packet);

        SocketServerUDP.clients.forEach((playerId, client) -> {
//            if (!player.getUniqueId().equals(playerId)) {
//                VoicePlayer p = VoiceServer.getAPI().getPlayerManager().getByUniqueId(playerId);
//
//                if (maxDistanceSquared > 0) {
//                    if (!player.inRadius(p, maxDistanceSquared)) {
//                        return;
//                    }
//                }
//
//                sendTo(
//                        VoiceServer.getAPI().getPlayerManager().getByUniqueId(playerId),
//                        bytes
//                );
//            }
            sendTo(
                    VoiceServer.getAPI().getPlayerManager().getByUniqueId(playerId),
                    bytes
            );
        });
    }
}
