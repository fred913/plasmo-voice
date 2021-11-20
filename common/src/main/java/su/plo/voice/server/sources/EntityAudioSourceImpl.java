package su.plo.voice.server.sources;

import su.plo.voice.api.entity.VoiceEntity;
import su.plo.voice.api.player.VoicePlayer;
import su.plo.voice.api.sources.EntityAudioSource;
import su.plo.voice.protocol.packets.udp.AudioSourceS2CPacket;
import su.plo.voice.server.VoiceServer;
import su.plo.voice.server.socket.SocketServerUDP;
import su.plo.voice.utils.AudioUtils;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class EntityAudioSourceImpl extends AbstractAudioSource implements EntityAudioSource {
    @Nonnull
    private final UUID entityId;

    public EntityAudioSourceImpl(int sourceId, @Nonnull UUID entityId) {
        super(sourceId);
        this.entityId = entityId;
    }

    public EntityAudioSourceImpl(int sourceId, @Nonnull UUID entityId, Application mode, int bitrate) {
        super(sourceId, mode, bitrate);
        this.entityId = entityId;
    }

    @Override
    public void send(short[] samples, int distance) {
        byte[] bytes = encode(samples);
        if (sequenceNumber == null) {
            sequenceNumber = new AtomicLong(1);
        }

        sendUdpToListeners(
                new AudioSourceS2CPacket(getId(), (short) distance, bytes),
                System.currentTimeMillis(),
                sequenceNumber.getAndIncrement(),
                (short) distance
        );
    }

    @Override
    public void send(byte[] samples, boolean encoded, int distance, Long timestamp, long sequenceNumber) {
        if (!encoded) {
            samples = encode(AudioUtils.bytesToShorts(samples));
        }

        sendUdpToListeners(
                new AudioSourceS2CPacket(getId(), (short) distance, samples),
                timestamp,
                sequenceNumber,
                (short) distance
        );
    }

    @Override
    public void send(short[] samples, int distance, Long timestamp, long sequenceNumber) {
        byte[] bytes = encode(samples);

        sendUdpToListeners(
                new AudioSourceS2CPacket(getId(), (short) distance, bytes),
                timestamp,
                sequenceNumber,
                (short) distance
        );
    }

    @Override
    public @Nonnull VoiceEntity getEntity() {
        return VoiceServer.getAPI().getEntityManager().getByUniqueId(entityId);
    }

    @Override
    public List<VoicePlayer> getListeners(short distance) {
        List<VoicePlayer> listeners = new ArrayList<>();

        double maxDistanceSquared = distance * distance * 1.25F;
        SocketServerUDP.clients.forEach((uuid, client) -> {
            if (maxDistanceSquared > 0) {
                if (!client.getPlayer().inRadius(getEntity().getWorld(), getEntity().getPosition(), maxDistanceSquared)) {
                    return;
                }
            }

            listeners.add(client.getPlayer());
        });

        return listeners;
    }

    @Override
    public @Nonnull UUID getEntityId() {
        return entityId;
    }
}
