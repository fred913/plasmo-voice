package su.plo.voice.server.sources;

import lombok.Getter;
import lombok.Setter;
import su.plo.voice.api.player.VoicePlayer;
import su.plo.voice.api.sources.AudioSource;
import su.plo.voice.api.sources.AudioSourceHandler;
import su.plo.voice.opus.Encoder;
import su.plo.voice.protocol.packets.Packet;
import su.plo.voice.protocol.packets.tcp.AudioEndSourceS2CPacket;
import su.plo.voice.protocol.packets.tcp.ClientTcpPacketListener;
import su.plo.voice.protocol.packets.tcp.MessageTcp;
import su.plo.voice.protocol.packets.udp.AbstractPacketUdp;
import su.plo.voice.protocol.packets.udp.MessageUdp;
import su.plo.voice.server.VoiceServer;
import su.plo.voice.server.socket.SocketClientUDP;
import su.plo.voice.server.socket.SocketServerUDP;
import su.plo.voice.utils.AudioUtils;

import javax.annotation.Nullable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public abstract class AbstractAudioSource implements AudioSource {
    protected AtomicLong sequenceNumber;
    @Getter
    private final int id;
    @Nullable
    private Encoder encoder;
    @Getter
    @Setter
    protected boolean visible = true;

    @Nullable
    protected AudioSourceHandler handler;

    @Nullable
    protected ScheduledExecutorService scheduler;

    public AbstractAudioSource(int id) {
        this.id = id;
    }

    public AbstractAudioSource(int id, Application mode, int bitrate) {
        this.id = id;
        this.encoder = new Encoder(mode.value(), bitrate);
    }

    protected void sendUdpToListeners(AbstractPacketUdp packet,
                                      Long timestamp, long sequenceNumber, short distance) {
        byte[] bytes = MessageUdp.write((Packet) packet, timestamp, sequenceNumber);
        for (VoicePlayer listener : getListeners(distance)) {
            SocketClientUDP client = SocketServerUDP.clients.get(listener.getUniqueId());
            if (client != null) {
                SocketServerUDP.sendTo(bytes, client);
            }
        }
    }

    protected void sendTcpToListeners(Packet<ClientTcpPacketListener> packet, short distance) {
        byte[] bytes = MessageTcp.write(packet);
        for (VoicePlayer listener : getListeners(distance)) {
            VoiceServer.getNetwork().sendTo(listener, bytes);
        }
    }

    @Override
    public void sendEnd(int distance, long sequenceNumber) {
        sendTcpToListeners(new AudioEndSourceS2CPacket(getId(), sequenceNumber), (short) distance);
    }

    @Override
    public void sendEnd(int distance) {
        if (sequenceNumber == null) {
            sequenceNumber = new AtomicLong();
        }

        sendTcpToListeners(new AudioEndSourceS2CPacket(getId(), sequenceNumber.getAndIncrement()), (short) distance);
    }

    @Override
    public void setApplication(Application mode) {
        if (encoder == null) {
            this.encoder = new Encoder(mode.value(), -1000);
        } else {
            encoder.setMode(mode.value());
        }
    }

    @Override
    public Application getApplication() {
        return encoder == null ? null : Application.of(encoder.getMode());
    }

    @Override
    public void setBitrate(int bitrate) {
        if (encoder == null) {
            this.encoder = new Encoder(Application.VOIP.value(), bitrate);
        } else {
            encoder.setBitrate(bitrate);
        }
    }

    @Override
    public int getBitrate() {
        return encoder == null ? -1 : encoder.getBitrate();
    }

    @Override
    public byte[] encode(short[] samples) {
        if (encoder == null) {
            this.encoder = new Encoder(Application.VOIP.value(), -1000);
        }

        return encoder.process(samples);
    }

    @Override
    public void setSourceHandler(AudioSourceHandler handler) {
        this.handler = handler;
        if (scheduler == null) {
            scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.scheduleAtFixedRate(() -> {
                if (handler.canProvide()) {
                    byte[] samples = handler.provide20MsAudio();
                    if (!handler.isOpusEncoded()) {
                        samples = encode(AudioUtils.bytesToShorts(samples));
                    }

                    if (sequenceNumber == null) {
                        sequenceNumber = new AtomicLong();
                    }

                    send(samples, true, handler.getDistance(), System.currentTimeMillis(), sequenceNumber.getAndIncrement());
                }
            }, 0L, 20L, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void close() {
        if (scheduler != null) {
            scheduler.shutdown();
        }

        if (encoder != null) {
            encoder.close();
        }
    }
}
