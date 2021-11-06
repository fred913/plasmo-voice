package su.plo.voice.client.socket;

import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import su.plo.voice.client.VoiceClient;
import su.plo.voice.client.gui.VoiceNotAvailableScreen;
import su.plo.voice.client.gui.VoiceSettingsScreen;
import su.plo.voice.client.sound.AbstractSoundQueue;
import su.plo.voice.client.sound.openal.OpenALPlayerQueue;
import su.plo.voice.protocol.data.VoiceClientInfo;
import su.plo.voice.protocol.packets.udp.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SocketClientUDPListener extends Thread implements ClientUdpPacketListener {
    private static final Minecraft client = Minecraft.getInstance();
    private final SocketClientUDP socket;
    public ConcurrentLinkedQueue<MessageUdp> queue = new ConcurrentLinkedQueue<>();
    public static final Map<Integer, AbstractSoundQueue> audioChannels = new HashMap<>();

    public SocketClientUDPListener(SocketClientUDP socket) {
        this.socket = socket;
    }

    public static void close(int sourceId) {
        audioChannels.remove(sourceId);
        VoiceClient.getNetwork().getTalking().remove(sourceId);
    }

    public static void closeAll() {
        // kill all queues to prevent possible problems
        audioChannels.values()
                .forEach(AbstractSoundQueue::close);
        audioChannels.clear();
        if (VoiceClient.getNetwork() != null) {
            VoiceClient.getNetwork().getTalking().clear();
        }
    }

    private void queuePacket(MessageUdp message) {
        AudioDirectS2CPacket packet = (AudioDirectS2CPacket) message.getPayload();

        AbstractSoundQueue ch = audioChannels.get(packet.getSourceId());
        if (ch == null) {
            if (packet instanceof AudioPlayerS2CPacket) {
                try {
                    VoiceClientInfo client = VoiceClient.getNetwork().getClientById(packet.getSourceId());
                    ch = new OpenALPlayerQueue(packet.getSourceId(), client);
                    ch.write(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            audioChannels.put(packet.getSourceId(), ch);
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
