package su.plo.voice.server.socket;

import lombok.Data;
import su.plo.voice.api.player.VoicePlayer;
import su.plo.voice.protocol.packets.tcp.ClientDisconnectedS2CPacket;
import su.plo.voice.protocol.packets.udp.*;
import su.plo.voice.server.VoiceServer;

import java.net.SocketAddress;

@Data
public class SocketClientUDP implements ServerUdpPacketListener {
    private final VoicePlayer player;

    private final SocketAddress address;
    private long keepAlive;
    private long sentKeepAlive;

    public SocketClientUDP(VoicePlayer player, SocketAddress address) {
        this.player = player;
        this.address = address;
        this.keepAlive = System.currentTimeMillis();
    }

    public VoicePlayer getPlayer() {
        return player;
    }

    public void close() {
        if (SocketServerUDP.clients.containsKey(player.getUniqueId())) {
            if (!VoiceServer.getInstance().getConfig().getBoolean("disable_logs")) {
                VoicePlayer player = this.getPlayer();
                VoiceServer.LOGGER.info("Remove client UDP: " + player.getName());
            }
            SocketServerUDP.clients.remove(player.getUniqueId());

            VoiceServer.getNetwork().sendToClients(
                    new ClientDisconnectedS2CPacket(player.getUniqueId()),
                    null
            );
        }

        if (player.getId() != 0) {
            VoiceServer.getSources().unregister(player.getId());
        }
    }

    @Override
    public void handle(AudioDirectC2SPacket packet) {

    }

    @Override
    public void handle(AudioPlayerC2SPacket packet) {
        VoicePlayer player = getPlayer();
        if (player == null) {
            return;
        }

        if (VoiceServer.getAPI().isMuted(player.getUniqueId())) {
            return;
        }

        if (!player.hasPermission("voice.speak")) {
            return;
        }


        if (packet.getDistance() > VoiceServer.getServerConfig().getMaxDistance() && (
                !player.hasPermission("voice.priority") || packet.getDistance() > VoiceServer.getServerConfig().getMaxPriorityDistance()
                )) {
            return;
        }

        AudioPlayerS2CPacket serverPacket = new AudioPlayerS2CPacket(
                player.getId(),
                packet.getDistance(),
                packet.getData()
        );
        SocketServerUDP.sendToNearbyPlayers(
                serverPacket,
                packet.getMessage().getTimestamp(), packet.getMessage().getSequenceNumber(),
                player, packet.getDistance()
        );
    }

    @Override
    public void handle(AudioRawC2SPacket packet) {

    }

    @Override
    public void handle(PingC2SPacket packet) {
        setKeepAlive(System.currentTimeMillis());
    }
}
