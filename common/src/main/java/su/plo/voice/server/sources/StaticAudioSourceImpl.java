package su.plo.voice.server.sources;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.api.Pos3d;
import su.plo.voice.api.player.VoicePlayer;
import su.plo.voice.api.sources.StaticAudioSource;
import su.plo.voice.protocol.packets.tcp.MessageTcp;
import su.plo.voice.protocol.packets.tcp.SourceInfoS2CPacket;
import su.plo.voice.protocol.packets.udp.AudioSourceS2CPacket;
import su.plo.voice.protocol.sources.StaticSourceInfo;
import su.plo.voice.server.VoiceServer;
import su.plo.voice.server.socket.SocketServerUDP;
import su.plo.voice.utils.AudioUtils;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class StaticAudioSourceImpl extends AbstractAudioSource implements StaticAudioSource {
    @Getter
    @Nonnull
    private String worldName;
    @Getter
    @Nonnull
    private Pos3d position;
    @Getter
    @Nullable
    private Pos3d direction;
    @Getter
    private int angle;

    private final Map<VoicePlayer, Long> listeners = new ConcurrentHashMap<>();

    public StaticAudioSourceImpl(int sourceId, @Nonnull String worldName, @Nonnull Pos3d position) {
        super(sourceId);
        this.worldName = worldName;
        this.position = position;
    }

    public StaticAudioSourceImpl(int sourceId, @Nonnull String worldName, @Nonnull Pos3d position,
                                 Application mode, int bitrate) {
        super(sourceId, mode, bitrate);
        this.worldName = worldName;
        this.position = position;
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
    public void send(byte[] samples, boolean encoded, int distance, @Nullable Long timestamp, long sequenceNumber) {
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
    public List<VoicePlayer> getListeners(short distance) {
        List<VoicePlayer> listeners = new ArrayList<>();

        double maxDistanceSquared = distance * distance * 1.25F;
        SocketServerUDP.clients.forEach((uuid, client) -> {
            if (maxDistanceSquared > 0) {
                if (!client.getPlayer().inRadius(worldName, position, maxDistanceSquared)) {
                    return;
                }
            }

            listeners.add(client.getPlayer());
            this.listeners.put(client.getPlayer(), System.currentTimeMillis());
        });

        // todo код говно переделай
        for (Map.Entry<VoicePlayer, Long> entry : this.listeners.entrySet()) {
            if (System.currentTimeMillis() - entry.getValue() > 30_000L) {
                listeners.remove( entry.getKey());
            }
        }

        return listeners;
    }

    @Override
    public StaticAudioSource setPosition(@Nonnull String worldName, @Nonnull Pos3d position) {
        this.worldName = worldName;
        this.position = position;
        onSourceUpdate();
        return this;
    }

    @Override
    public StaticAudioSource setAngle(@Nonnull Pos3d direction, int angle) {
        this.angle = angle;
        onSourceUpdate();
        return this;
    }

    @Override
    public StaticAudioSource setDirection(@Nullable Pos3d direction) {
        this.direction = direction;
        onSourceUpdate();
        return this;
    }

    private void onSourceUpdate() {
        byte[] bytes = MessageTcp.write(new SourceInfoS2CPacket(
                new StaticSourceInfo(
                        getId(),
                        position,
                        direction,
                        angle,
                        visible
                )
        ));

        for (Map.Entry<VoicePlayer, Long> entry : listeners.entrySet()) {
            VoicePlayer player = entry.getKey();

            if (System.currentTimeMillis() - entry.getValue() > 30_000L) {
                listeners.remove(player);
                continue;
            }

            VoiceServer.getNetwork().sendTo(player, bytes);
        }
    }
}
