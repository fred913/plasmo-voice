package su.plo.voice.server.sources;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.api.player.VoicePlayer;
import su.plo.voice.api.sources.DirectAudioSource;
import su.plo.voice.protocol.packets.Packet;
import su.plo.voice.protocol.packets.tcp.ClientTcpPacketListener;
import su.plo.voice.protocol.packets.tcp.MessageTcp;
import su.plo.voice.protocol.packets.udp.AbstractPacketUdp;
import su.plo.voice.protocol.packets.udp.AudioDirectS2CPacket;
import su.plo.voice.protocol.packets.udp.AudioPlayerS2CPacket;
import su.plo.voice.protocol.packets.udp.MessageUdp;
import su.plo.voice.server.VoiceServer;
import su.plo.voice.server.socket.SocketClientUDP;
import su.plo.voice.server.socket.SocketServerUDP;
import su.plo.voice.utils.AudioUtils;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class DirectAudioSourceImpl extends AbstractAudioSource implements DirectAudioSource {
    private final UUID playerId;

    public DirectAudioSourceImpl(int sourceId, @Nonnull UUID playerId) {
        super(sourceId);
        this.playerId = playerId;
    }

    public DirectAudioSourceImpl(int sourceId, @Nonnull UUID playerId, Application mode, int bitrate) {
        super(sourceId, mode, bitrate);
        this.playerId = playerId;
    }

    @Override
    public void send(short[] samples, int distance) {
        byte[] bytes = encode(samples);
        if (sequenceNumber == null) {
            sequenceNumber = new AtomicLong(1);
        }

        sendUdpToListeners(
                new AudioDirectS2CPacket(getId(), bytes),
                System.currentTimeMillis(),
                sequenceNumber.getAndIncrement(),
                (short) distance
        );
    }

    @Override
    public void send(byte[] samples, boolean encoded, int distance, @Nullable Long timestamp, long sequenceNumber) {
        if (!encoded) {
            samples = encode(AudioUtils.bytesToShorts(samples));
        }

        sendUdpToListeners(
                new AudioDirectS2CPacket(getId(), samples),
                timestamp,
                sequenceNumber,
                (short) distance
        );
    }

    @Override
    public void send(short[] samples, int distance, Long timestamp, long sequenceNumber) {
        byte[] bytes = encode(samples);

        sendUdpToListeners(
                new AudioPlayerS2CPacket(getId(), (short) distance, bytes),
                timestamp,
                sequenceNumber,
                (short) distance
        );
    }

    @Override
    public @Nonnull
    @NotNull VoicePlayer getPlayer() {
        return VoiceServer.getAPI().getPlayerManager().getByUniqueId(playerId);
    }

    @Override
    public List<VoicePlayer> getListeners(short distance) {
        return ImmutableList.of(getPlayer());
    }

    @Override
    protected void sendUdpToListeners(AbstractPacketUdp packet, Long timestamp, long sequenceNumber, short distance) {
        SocketClientUDP client = SocketServerUDP.clients.get(playerId);
        if (client != null) {
            SocketServerUDP.sendTo(
                    MessageUdp.write((Packet) packet, timestamp, sequenceNumber),
                    client
            );
        }
    }

    @Override
    protected void sendTcpToListeners(Packet<ClientTcpPacketListener> packet, short distance) {
        VoiceServer.getNetwork().sendTo(
                getPlayer(),
                MessageTcp.write(packet)
        );
    }
}
