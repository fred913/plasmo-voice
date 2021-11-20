package su.plo.voice.client.socket;

import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import su.plo.voice.client.VoiceClient;
import su.plo.voice.client.gui.VoiceNotAvailableScreen;
import su.plo.voice.client.gui.VoiceSettingsScreen;
import su.plo.voice.client.sound.AbstractAudioSource;
import su.plo.voice.client.sound.openal.DirectAudioSource;
import su.plo.voice.client.sound.openal.EntityAudioSource;
import su.plo.voice.client.sound.openal.PlayerAudioSource;
import su.plo.voice.client.sound.openal.StaticAudioSource;
import su.plo.voice.protocol.data.VoiceClientInfo;
import su.plo.voice.protocol.packets.udp.*;
import su.plo.voice.protocol.sources.EntitySourceInfo;
import su.plo.voice.protocol.sources.SourceInfo;
import su.plo.voice.protocol.sources.StaticSourceInfo;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SocketClientUDPListener extends Thread implements ClientUdpPacketListener {
    private static final Minecraft client = Minecraft.getInstance();
    private final SocketClientUDP socket;
    public ConcurrentLinkedQueue<MessageUdp> queue = new ConcurrentLinkedQueue<>();
    public static final Map<Integer, AbstractAudioSource> sources = new HashMap<>();

    public SocketClientUDPListener(SocketClientUDP socket) {
        this.socket = socket;
    }

    public static void close(int sourceId) {
        sources.remove(sourceId);
        if (VoiceClient.isConnected()) {
            VoiceClient.getNetwork().getTalking().remove(sourceId);
        }
    }

    public static void closeAll() {
        // kill all queues to prevent possible problems
        sources.values()
                .forEach(AbstractAudioSource::close);
        sources.clear();
        if (VoiceClient.getNetwork() != null) {
            VoiceClient.getNetwork().getTalking().clear();
        }
    }

    private void queuePacket(MessageUdp message) {
        AudioDirectS2CPacket packet = (AudioDirectS2CPacket) message.getPayload();

        AbstractAudioSource ch = sources.get(packet.getSourceId());
        if (ch == null) {
            try {
                if (packet instanceof AudioPlayerS2CPacket) {
                    VoiceClientInfo client = VoiceClient.getNetwork().getClientById(packet.getSourceId());
                    ch = new PlayerAudioSource(packet.getSourceId(), client);
                    ch.write(message);
                } else if (packet instanceof AudioSourceS2CPacket) {
                    SourceInfo info = VoiceClient.getNetwork().getSourceInfo(packet.getSourceId());
                    if (info instanceof StaticSourceInfo) {
                        ch = new StaticAudioSource(packet.getSourceId(), (StaticSourceInfo) info);
                        ch.write(message);
                    } else if (info instanceof EntitySourceInfo) {
                        ch = new EntityAudioSource(packet.getSourceId(), (EntitySourceInfo) info);
                        ch.write(message);
                    }
                } else {
                    ch = new DirectAudioSource(packet.getSourceId());
                    ch.write(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            sources.put(packet.getSourceId(), ch);
        } else {
            ch.write(message);
        }
    }

    @Override
    public void run() {
        while (!socket.isClosed()) {
            if (!this.queue.isEmpty()) {
                MessageUdp message = this.queue.poll();
                message.getPayload().handle(this);
            } else {
                synchronized (this) {
                    try {
                        this.wait();
                    } catch (InterruptedException ignored) {
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void handle(AudioDirectS2CPacket packet) {
        if (VoiceClient.getClientConfig().speakerMuted.get()) {
            return;
        }

        VoiceClient.getNetwork().getTalking().put(
                packet.getSourceId(),
                false
        );

        queuePacket(packet.getMessage());
    }

    @SneakyThrows
    @Override
    public void handle(AudioPlayerS2CPacket packet) {
        if (VoiceClient.getClientConfig().speakerMuted.get()) {
            return;
        }

        if (VoiceClient.getNetwork().isPlayerMuted(packet.getSourceId())) {
            return;
        }

        VoiceClient.getNetwork().getTalking().put(
                packet.getSourceId(),
                packet.getDistance() > VoiceClient.getServerConfig().getMaxDistance()
        );

        queuePacket(packet.getMessage());
    }

    @Override
    public void handle(AudioRawS2CPacket packet) {
    }

    @Override
    public void handle(AudioSourceS2CPacket packet) {
        if (VoiceClient.getClientConfig().speakerMuted.get()) {
            return;
        }

        VoiceClient.getNetwork().getTalking().put(
                packet.getSourceId(),
                packet.getDistance() > VoiceClient.getServerConfig().getMaxDistance()
        );

        queuePacket(packet.getMessage());
    }

    @Override
    public void handle(AuthS2CPacket payload) {
        if (!socket.authorized) {
            VoiceClient.LOGGER.info("Connected to UDP");
            socket.authorized = true;
            VoiceClient.recorder.start();

            if (client.screen instanceof VoiceNotAvailableScreen) {
                client.execute(() ->
                        client.setScreen(new VoiceSettingsScreen())
                );
            }
        }
    }

    @Override
    public void handle(PingS2CPacket packet) {
        socket.keepAlive = System.currentTimeMillis();
        socket.ping.timedOut = false;
        socket.send(new PingC2SPacket(), 0L);
    }
}
