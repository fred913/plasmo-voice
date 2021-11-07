package su.plo.voice.client.sound;

import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import su.plo.voice.client.VoiceClient;
import su.plo.voice.client.gui.VoiceSettingsScreen;
import su.plo.voice.client.sound.activation.Activation;
import su.plo.voice.client.sound.activation.ActivationPTT;
import su.plo.voice.client.sound.activation.ActivationVoice;
import su.plo.voice.client.sound.capture.AlCaptureDevice;
import su.plo.voice.client.sound.capture.CaptureDevice;
import su.plo.voice.client.sound.capture.JavaxCaptureDevice;
import su.plo.voice.client.sound.opus.Encoder;
import su.plo.voice.client.utils.AudioUtils;
import su.plo.voice.protocol.packets.tcp.AudioEndPlayerC2SPacket;
import su.plo.voice.protocol.packets.udp.AudioPlayerC2SPacket;
import su.plo.voice.rnnoise.Denoiser;
import tomp2p.opuswrapper.Opus;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class AudioCapture implements Runnable {
    private final Minecraft client = Minecraft.getInstance();

    @Getter
    private static final int mtuSize = 1024;
    @Getter
    private static int sampleRate = 24000;
    @Getter
    private static int frameSize = (sampleRate / 1000) * 20;
    @Getter
    private static AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, false);

    @Getter
    private boolean available;
    @Getter
    private Thread thread;

    private Activation activation;
    private CaptureDevice microphone;
    private Encoder encoder;

    // RNNoise
    private Denoiser denoiser;
    // Limiter to fix RNNoise clipping
    private final Limiter limiter = new Limiter(-6.0F);

    private final VolumeAdjuster adjuster = new VolumeAdjuster();

    private int jopusMode;

    private long sequenceNumber = 0L;

    public AudioCapture() {
        if (VoiceClient.getClientConfig().rnNoise.get()) {
            this.denoiser = new Denoiser();
        }
        if (!VoiceClient.getClientConfig().voiceActivation.get() ||
                !VoiceClient.getServerConfig().isActivation()) {
            updateActivation(Activation.Type.PushToTalk);
        } else {
            updateActivation(Activation.Type.Voice);
        }

        this.jopusMode = Opus.OPUS_APPLICATION_VOIP;
//        if (VoiceClient.getClientConfig().jopusMode.get().equals("audio")) {
//            jopusMode = Opus.OPUS_APPLICATION_AUDIO;
//        } else if (VoiceClient.getClientConfig().jopusMode.get().equals("low-delay")) {
//            jopusMode = Opus.OPUS_APPLICATION_RESTRICTED_LOWDELAY;
//        } else {
//            jopusMode = Opus.OPUS_APPLICATION_VOIP;
//        }
    }

//    public synchronized void updateJopusMode() {
//        if (VoiceClient.getClientConfig().jopusMode.get().equals("audio")) {
//            jopusMode = Opus.OPUS_APPLICATION_AUDIO;
//        } else if (VoiceClient.getClientConfig().jopusMode.get().equals("low-delay")) {
//            jopusMode = Opus.OPUS_APPLICATION_RESTRICTED_LOWDELAY;
//        } else {
//            jopusMode = Opus.OPUS_APPLICATION_VOIP;
//        }
//
//        if (this.encoder != null) {
//            this.encoder.close();
//        }
//        System.out.println(jopusMode);
//        this.encoder = new OpusEncoder(sampleRate, frameSize, mtuSize, jopusMode);
//    }

    public synchronized void toggleRnNoise() {
        if (denoiser != null) {
            denoiser.close();

            this.denoiser = null;
        } else {
            this.denoiser = new Denoiser();
        }
    }

    public void updateActivation(Activation.Type type) {
        switch (type) {
            case PushToTalk -> activation = new ActivationPTT();
            case Voice -> activation = new ActivationVoice();
        }
    }

    public void updateBitRate(int bitRate) {
        encoder.setBitRate(bitRate);
    }

    /**
     * Method to update sample rate after vreload sent by server
     * @param rate New sample rate
     */
    public void updateSampleRate(int rate) {
        if (rate == AudioCapture.getSampleRate()) {
            return;
        }

        if (rate != 8000 && rate != 12000 && rate != 24000 && rate != 48000) {
            VoiceClient.LOGGER.info("Incorrect sample rate");
            return;
        }

        if (thread != null) {
            this.waitForClose().thenRun(() -> updateSampleRateSync(rate));
        } else {
            updateSampleRateSync(rate);
        }
    }

    private void updateSampleRateSync(int rate) {
        format = new AudioFormat(rate, 16, 1, true, false);
        sampleRate = rate;
        frameSize = (sampleRate / 1000) * 20;

        if (encoder != null) {
            encoder.close();
        }
        this.encoder = new Encoder(jopusMode, VoiceClient.getClientConfig().bitrate.get() * 1000);

        if (VoiceClient.isConnected()) {
            start();
        }
    }

    /**
     * Closes capture device and opus encoder
     */
    private void close() {
        this.sequenceNumber = 0L;
        if (encoder != null) {
            encoder.close();
        }

        if (microphone.isOpen()) {
            microphone.stop();
            microphone.close();
        }

        synchronized (this) {
            this.notifyAll();
        }
    }

    public void interrupt() {
        if (thread != null) {
            thread.interrupt();
        }
    }

    public void run() {
        if (microphone != null && microphone.isOpen()) {
            synchronized (this) {
                try {
                    this.wait();
                } catch (InterruptedException ignored) {
                    return;
                }
            }
        }

        this.available = true;

        // open capture device
        try {
            // sometimes openal mic doesn't work at all,
            // so I just made old javax capture system
            if (VoiceClient.getClientConfig().javaxCapture.get()) {
                VoiceClient.LOGGER.debug("Using javax capture device");
                microphone = new JavaxCaptureDevice();
            } else {
                microphone = new AlCaptureDevice();
            }

            microphone.open();
            VoiceClient.LOGGER.debug("Capture device opened");
        } catch (IllegalStateException e) {
            VoiceClient.LOGGER.debug("Failed to open OpenAL capture device, falling back to javax capturing");
            if (microphone instanceof AlCaptureDevice) {
                VoiceClient.getClientConfig().javaxCapture.set(true);

                microphone = new JavaxCaptureDevice();

                try {
                    microphone.open();
                } catch (IllegalStateException ignored) {
                    VoiceClient.getClientConfig().javaxCapture.set(false);
                    VoiceClient.LOGGER.error("Capture device not available on this system");
                    this.available = false;
                    return;
                }
            }
        }

        if (encoder == null || !encoder.isOpen()) {
            this.encoder = new Encoder(jopusMode, VoiceClient.getClientConfig().bitrate.get() * 1000);
        }

        while (!thread.isInterrupted()) {
            final LocalPlayer player = client.player;
            if (player == null) {
                break;
            }

            short[] samples = capture();
            if (!VoiceClient.isConnected()) {
                break;
            }

            // muted
            if ((VoiceClient.getClientConfig().microphoneMuted.get() || VoiceClient.getClientConfig().speakerMuted.get()) ||
                    VoiceClient.getNetwork().isPlayerMuted(player.getUUID())) {
                VoiceClient.setSpeaking(false);
                VoiceClient.setSpeakingPriority(false);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                    break;
                }
            }

            if (samples == null) {
                continue;
            }

            Activation.Result result = activation.process(samples);
            if (result.equals(Activation.Result.Process)) {
                send(samples);
            } else if (result.equals(Activation.Result.End)) {
                send(samples);
                sendEnd();
            }
        }

        this.close();
    }

    /**
     * Capture samples sync
     * @return captured samples
     */
    private synchronized short[] capture() {
        if (this.encoder == null || !this.encoder.isOpen()) {
            return null;
        }

        microphone.start();
        short[] samples = microphone.capture(frameSize);
        if (samples == null) {
            return null;
        }

        adjuster.adjust(samples, VoiceClient.getClientConfig().microphoneAmplification.get().floatValue());

        if (this.denoiser != null) {
            samples = limiter.limit(samples);

            float[] floats = AudioUtils.shortsToFloats(samples);

            samples = AudioUtils.floatsToShorts(this.denoiser.process(floats));
        }

        if (client.screen instanceof VoiceSettingsScreen screen) {
            screen.setMicrophoneValue(samples);
        }

        return samples;
    }

    private void send(short[] samples) {
        if (VoiceClient.isMicrophoneLoopback()) {
            return;
        }

        if (!VoiceClient.isConnected()) {
            thread.interrupt();
            return;
        }

        VoiceClient.socketUDP.send(new AudioPlayerC2SPacket(
                getDistance(),
                encoder.process(samples)
        ), sequenceNumber++);
    }

    private void sendEnd() {
        if (!VoiceClient.isConnected()) {
            thread.interrupt();
            return;
        }

        encoder.reset();

        try {
            VoiceClient.getNetwork().sendToServer(new AudioEndPlayerC2SPacket(getDistance(), sequenceNumber++));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private short getDistance() {
        return VoiceClient.isSpeakingPriority()
                ? VoiceClient.getServerConfig().getPriorityDistance()
                : VoiceClient.getServerConfig().getDistance();
    }

    /**
     * Starts capture thread
     */
    public void start() {
        if (thread != null) {
            this.waitForClose().thenRun(() -> {
                this.thread = new Thread(this, "Input Device Recorder");
                thread.start();
            });
        } else {
            this.thread = new Thread(this, "Input Device Recorder");
            thread.start();
        }
    }

    /**
     * Waiting capture device to close
     */
    // todo is it necessary at all?
    public CompletableFuture<Void> waitForClose() {
        return CompletableFuture.runAsync(() -> {
            thread.interrupt();

            synchronized (this) {
                try {
                    this.wait(1000L); // wait for 1 sec and just ignore it if notify not called
                } catch (InterruptedException ignored) {
                }
            }
        });
    }
}
