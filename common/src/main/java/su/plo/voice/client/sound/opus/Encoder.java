package su.plo.voice.client.sound.opus;

import com.sun.jna.ptr.PointerByReference;
import su.plo.voice.client.VoiceClient;
import su.plo.voice.client.sound.AudioCapture;
import tomp2p.opuswrapper.Opus;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

public class Encoder {
    private final ByteBuffer buffer = ByteBuffer.allocate(1024);
    private PointerByReference pointer;

    public Encoder(int mode, int bitrate) {
        IntBuffer error = IntBuffer.allocate(1);
        pointer = Opus.INSTANCE.opus_encoder_create(AudioCapture.getSampleRate(), 1, mode, error);
        if (error.get() != Opus.OPUS_OK) {
            VoiceClient.LOGGER.error("Failed to initialize opus encoder: {}", error.get());
        }

        setBitRate(bitrate);
        VoiceClient.LOGGER.debug("Opus encoder initialized with mode: {} and bitrate: {}", mode, bitrate);
    }

    /**
     * Update bitrate in opus encoder
     * @param bitrate New bitrate
     */
    public void setBitRate(int bitrate) {
        Opus.INSTANCE.opus_encoder_ctl(pointer, Opus.OPUS_SET_BITRATE_REQUEST, bitrate);

        IntBuffer request = IntBuffer.allocate(1);
        Opus.INSTANCE.opus_encoder_ctl(pointer, Opus.OPUS_GET_BITRATE_REQUEST, request);
        VoiceClient.LOGGER.debug("Update opus encoder bitrate to {}", request.get());
    }

    public byte[] process(short[] samples) {
        ShortBuffer nonEncodedBuffer = ShortBuffer.wrap(samples);
        buffer.clear();

        int encodedLength = Opus.INSTANCE.opus_encode(pointer, nonEncodedBuffer, AudioCapture.getFrameSize(), buffer, buffer.capacity());
        if (encodedLength < 0) {
            VoiceClient.LOGGER.error("Failed to encode opus data: {}", encodedLength);
            return null;
        }

        byte[] encodedSamples = new byte[encodedLength];
        buffer.get(encodedSamples);
        return encodedSamples;
    }

    public void reset() {
        if (isOpen()) {
            Opus.INSTANCE.opus_encoder_ctl(pointer, Opus.INSTANCE.OPUS_RESET_STATE);
        }
    }

    public void close() {
        if (!isOpen()) {
            VoiceClient.LOGGER.error("Opus encoder already closed");
            return;
        }

        VoiceClient.LOGGER.debug("Destroy opus encoder");
        Opus.INSTANCE.opus_encoder_destroy(pointer);
        this.pointer = null;
    }

    public boolean isOpen() {
        return pointer != null;
    }
}