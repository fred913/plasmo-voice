package su.plo.voice.server.sources;

import su.plo.voice.api.player.VoicePlayer;
import su.plo.voice.api.sources.PlayerAudioSource;
import su.plo.voice.protocol.packets.udp.AudioPlayerS2CPacket;
import su.plo.voice.server.VoiceServer;
import su.plo.voice.server.socket.SocketServerUDP;
import su.plo.voice.utils.AudioUtils;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class PlayerAudioSourceImpl extends AbstractAudioSource implements PlayerAudioSource {
    private final UUID playerId;

    public PlayerAudioSourceImpl(int sourceId, UUID playerId) {
        super(sourceId);
        this.playerId = playerId;
    }

    @Override
    public void send(short[] samples, int distance) {
        byte[] bytes = encode(samples);
        if (sequenceNumber == null) {
            sequenceNumber = new AtomicLong(1);
        }

        sendUdpToListeners(
                new AudioPlayerS2CPacket(getId(), (short) distance, bytes),
                System.currentTimeMillis(),
                sequenceNumber.getAndIncrement(),
                (short) distance
        );
    }

    @Override
    public void send(byte[] samples, boolean encoded, int distance, @org.jetbrains.annotations.Nullable Long timestamp, long sequenceNumber) {
        if (!encoded) {
            samples = encode(AudioUtils.bytesToShorts(samples));
        }

        sendUdpToListeners(
                new AudioPlayerS2CPacket(getId(), (short) distance, samples),
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
    public @Nonnull VoicePlayer getPlayer() {
        return VoiceServer.getAPI().getPlayerManager().getByUniqueId(playerId);
    }

    @Override
    public List<VoicePlayer> getListeners(short distance) {
        List<VoicePlayer> listeners = new ArrayList<>();

        VoicePlayer player = getPlayer();
        double maxDistanceSquared = distance * distance * 1.25F;

        SocketServerUDP.clients.forEach((uuid, client) -> {
//            if (!player.getUniqueId().equals(uuid)) {
//                if (maxDistanceSquared > 0) {
//                    if (!player.inRadius(client.getPlayer().getWorld(), client.getPlayer().getPosition(), maxDistanceSquared)) {
//                        return;
//                    }
//                }
//
//                listeners.add(client.getPlayer());
//            }
            listeners.add(client.getPlayer());
        });

        return listeners;
    }
}
