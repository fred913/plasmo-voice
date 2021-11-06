package su.plo.voice.server.network;

import su.plo.voice.api.player.VoicePlayer;
import su.plo.voice.protocol.packets.Packet;
import su.plo.voice.protocol.packets.tcp.ClientTcpPacketListener;
import su.plo.voice.protocol.packets.tcp.ConnectS2CPacket;
import su.plo.voice.server.VoiceServer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class ServerNetworkHandler {
    protected final HashMap<UUID, ServerNetworkListener> listeners = new HashMap<>();
    protected final HashMap<UUID, UUID> playerToken = new HashMap<>();

    protected synchronized ServerNetworkListener getOrCreateListener(VoicePlayer player) {
        ServerNetworkListener listener = listeners.get(player.getUniqueId());
        if (listener == null) {
            listener = new ServerNetworkListener(player);
            listeners.put(player.getUniqueId(), listener);
        }

        return listener;
    }

    public synchronized void updatePlayer(VoicePlayer player) {
        if (listeners.containsKey(player.getUniqueId())) {
            ServerNetworkListener listener = listeners.get(player.getUniqueId());
            listener.setPlayer(player);
        }
    }

    public synchronized UUID findByToken(String token) {
        UUID playerId = null;
        for (Map.Entry<UUID, UUID> entry : playerToken.entrySet()) {
            if (entry.getValue().toString().equals(token)) {
                playerId = entry.getKey();
                break;
            }
        }

        if (playerId != null) {
            playerToken.remove(playerId);
        }

        return playerId;
    }

    public synchronized void start() {
    }

    public synchronized void close() {
        playerToken.clear();
    }

    public void reconnectClient(VoicePlayer player) {
        UUID token = UUID.randomUUID();
        playerToken.put(player.getUniqueId(), token);

        sendTo(player,
                new ConnectS2CPacket(
                        token.toString(),
                        VoiceServer.getServerConfig().getProxyIp() != null && !VoiceServer.getServerConfig().getProxyIp().isEmpty()
                                ? VoiceServer.getServerConfig().getProxyIp()
                                : VoiceServer.getServerConfig().getIp(),
                        VoiceServer.getServerConfig().getProxyPort() != 0
                                ? VoiceServer.getServerConfig().getProxyPort()
                                : VoiceServer.getServerConfig().getPort()
                )
        );
    }

    /**
     * Send packet to the player
     */
    public abstract void sendTo(VoicePlayer player, Packet<ClientTcpPacketListener> packet);

    /**
     * Send bytes to the player
     *
     * @param bytes packet bytes
     */
    public abstract void sendTo(VoicePlayer player, byte[] bytes);

    /**
     * Send packet to all clients with voice chat
     * @param packet S2C packet
     * @param player Игрок которому не будет отправлен пакет (отправитель пакета)
     */
    public abstract void sendToClients(Packet<ClientTcpPacketListener> packet, VoicePlayer player);

    /**
     * Send packet to all clients with voice chat in provided radius
     * @param packet S2C packet
     * @param player Игрок которому не будет отправлен пакет (отправитель пакета)
     * @param maxDistance Радиус
     */
    public abstract void sendToNearbyClients(Packet<ClientTcpPacketListener> packet, VoicePlayer player, double maxDistance);
}
