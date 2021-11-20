package su.plo.voice.server.socket;

import lombok.Data;
import su.plo.voice.api.player.VoicePlayer;
import su.plo.voice.api.sources.AudioSource;
import su.plo.voice.protocol.packets.tcp.ClientDisconnectedS2CPacket;
import su.plo.voice.protocol.packets.udp.*;
import su.plo.voice.server.VoiceServer;

import javax.annotation.Nonnull;
import java.net.SocketAddress;

@Data
public class SocketClientUDP implements ServerUdpPacketListener {
    private final VoicePlayer player;
    private final AudioSource source;

    private final SocketAddress address;
    private long keepAlive;
    private long sentKeepAlive;

    public SocketClientUDP(@Nonnull VoicePlayer player, @Nonnull AudioSource source, @Nonnull SocketAddress address) {
        this.player = player;
        this.source = source;
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

        VoiceServer.getAPI().getSourceManager().destroy(source.getId());
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

        source.send(
                packet.getData(), true, packet.getDistance(),
                packet.getMessage().getTimestamp(), packet.getMessage().getSequenceNumber()
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
