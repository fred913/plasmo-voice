package su.plo.voice.client.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.openal.EXTThreadLocalContext;
import su.plo.voice.client.VoiceClient;
import su.plo.voice.client.gui.VoiceSettingsScreen;
import su.plo.voice.client.socket.SocketClientUDPListener;
import su.plo.voice.client.sound.clock.RtpClock;
import su.plo.voice.client.sound.clock.WallClock;
import su.plo.voice.client.sound.openal.AlUtil;
import su.plo.voice.client.sound.openal.CustomSource;
import su.plo.voice.client.sound.opus.Decoder;
import su.plo.voice.protocol.packets.udp.AudioRawS2CPacket;
import su.plo.voice.protocol.packets.udp.MessageUdp;

public abstract class AbstractAudioSource extends Thread {
    protected final Minecraft minecraft = Minecraft.getInstance();

    protected final FixedJitterBuffer buffer;
    public final int sourceId;

    /**
     * Audio synchronization
     */
    protected final WallClock wallClock = new WallClock();
    protected final RtpClock rtpClock = new RtpClock(wallClock);

    protected CustomSource source;
    protected long lastPacketTime;
    protected long sequenceNumber;
    protected long endSequnceNumber;

    protected Decoder decoder;
    protected double lastOcclusion = -1;

    protected final Compressor compressor = new Compressor();

    public AbstractAudioSource(int sourceId) {
        this.sourceId = sourceId;
//        this.lastPacketTime = System.currentTimeMillis() - 300L;
        this.sequenceNumber = -1L;
        this.decoder = new Decoder();

        rtpClock.setClockRate(AudioCapture.getSampleRate());
        buffer = new FixedJitterBuffer(rtpClock, 40);
    }

    @Override
    public void run() {
        EXTThreadLocalContext.alcSetThreadContext(VoiceClient.getSoundEngine().getContextPointer());
        createSource();

        while (!isInterrupted()) {
            try {
                tick();
            } catch (InterruptedException e) {
                break;
            }
        }

        SocketClientUDPListener.sources.remove(sourceId);
        if(source != null) {
            source.close();
        }
        EXTThreadLocalContext.alcSetThreadContext(0L);
    }

    /**
     * ru_RU
     * Создает OpenAL источник
     */
    protected void createSource() {
        this.source = VoiceClient.getSoundEngine().createSource();
        this.source.setPitch(1.0F);
        this.source.setLooping(false);
        this.source.setRelative(false);
        if (VoiceClient.getClientConfig().directionalSources.get()) {
            this.source.setAngle(VoiceClient.getClientConfig().directionalSourcesAngle.get());
        }
        AlUtil.checkErrors("Create custom source");
    }

    /**
     * ru_RU
     * Основная функция loop'a
     */
    protected void tick() throws InterruptedException {
        if (isDead()) {
            this.interrupt();
            return;
        }

        MessageUdp pkt = buffer.read();
        if (pkt == null) {
            if (endSequnceNumber > 0L &&
                    ((sequenceNumber + 1 == endSequnceNumber) ||
                            (lastPacketTime > 0L && System.currentTimeMillis() - lastPacketTime > 1000L))) {
                reset();
            }

            sleep(10L);
            return;
        }

        AudioRawS2CPacket packet = (AudioRawS2CPacket) pkt.getPayload();

        lastPacketTime = packet.getMessage().getTimestamp() / 1000000L;
        if (!preProcess(packet)) {
            return;
        }

        if (!process(packet)) {
            return;
        }

        postProcess(packet);
    }

    /**
     * ru_RU
     * Вызывается до основной обработки пакета
     * В основном здесь проверки на мут
     *
     * @param packet
     *
     * @return true, если можно продолжать дальше
     */
    protected boolean preProcess(AudioRawS2CPacket packet) {
        if (VoiceClient.getClientConfig().speakerMuted.get() ||
                VoiceClient.getClientConfig().voiceVolume.get() == 0.0f) {
            return false;
        }

        if (minecraft.screen instanceof VoiceSettingsScreen screen && screen.getSource() != null) {
            return false;
        }

        return true;
    }

    /**
     * ru_RU
     * Тут основная "логика" источника:
     * обновление позиции, уровня звука и т.д.
     *
     * @return true, если можно продолжать дальше
     */
    abstract protected boolean process(AudioRawS2CPacket packet);

    /**
     * ru_RU
     * PLC, воспроизведение звука
     */
    protected void postProcess(AudioRawS2CPacket packet) {
        if (sequenceNumber >= 0) {
            // todo use neteq
            int packetsToCompensate = (int) (packet.getMessage().getSequenceNumber() - (sequenceNumber + 1));
            if (packetsToCompensate <= 4) {

                for (int i = 0; i < packetsToCompensate; i++) {
                    source.write(decoder.process(null));
                }
                if (packetsToCompensate > 0) {
                    VoiceClient.LOGGER.debug("Packets lost: {} (source id: {})", packetsToCompensate, sourceId);
                }
            }
        }

        short[] decoded = decoder.process(packet.getData());
        if (VoiceClient.getClientConfig().compressor.get()) {
            compressor.compress(decoded);
        }

        source.write(decoded);

        this.sequenceNumber = packet.getMessage().getSequenceNumber();
    }

    private void reset() {
        decoder.reset();
        buffer.reset();
        sequenceNumber = -1L;
        endSequnceNumber = -1L;
        lastOcclusion = -1;
    }

    public void end(long sequenceNumber) {
        this.endSequnceNumber = sequenceNumber;
    }

    /**
     * Пересчитывает коэффициент затухания
     */
    public double calculateOcclusion(Player localPlayer, Vec3 position) {
        double occlusion = Occlusion.getOccludedPercent(localPlayer.level, localPlayer, position);
        if(lastOcclusion >= 0) {
            if(occlusion > lastOcclusion) {
                lastOcclusion = Math.max(lastOcclusion + 0.05, 0.0D);
            } else {
                lastOcclusion = Math.max(lastOcclusion - 0.05, occlusion);
            }

            occlusion = lastOcclusion;
        }
        if(lastOcclusion == -1) {
            lastOcclusion = occlusion;
        }

        return occlusion;
    }

    public boolean isDead() {
        return lastPacketTime > 0L && System.currentTimeMillis() - lastPacketTime > 30_000L;
    }

    public void close() {
        SocketClientUDPListener.close(sourceId);
        decoder.close();
    }

    /**
     * ru_RU
     * Обновляет часы и пишет в житер буфер
     * @param message
     */
    public void write(MessageUdp message) {
        if (isDead()) {
            throw new IllegalStateException("Channel is dead");
        }

        if (lastPacketTime > 0) {
            wallClock.tick((message.getTimestamp() - lastPacketTime) * 1000000L);
        }

        buffer.write(message);
    }
}
