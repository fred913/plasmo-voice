package su.plo.voice.client.sound.opus;

import com.sun.jna.ptr.PointerByReference;
import su.plo.voice.client.VoiceClient;
import su.plo.voice.client.sound.AudioCapture;
import tomp2p.opuswrapper.Opus;

import java.nio.IntBuffer;
import java.nio.ShortBuffer;

public class Decoder {
    private final ShortBuffer buffer = ShortBuffer.allocate(1024);
    private PointerByReference pointer;

    public Decoder() {
        IntBuffer error = IntBuffer.allocate(1);
        pointer = Opus.INSTANCE.opus_decoder_create(AudioCapture.getSampleRate(), 1, error);
        if (error.get() != Opus.OPUS_OK) {
            VoiceClient.LOGGER.error("Failed to initialize opus decoder: {}", error.get());
        }

        VoiceClient.LOGGER.debug("Opus decoder initialized");
    }

    public short[] process(byte[] data) {
        if(!isOpen()) {
            return new short[0];
        }

        buffer.clear();

        int result;
        if (data == null) {
            result = Opus.INSTANCE.opus_decode(pointer, null, 0, buffer, AudioCapture.getFrameSize(), 0);
        } else {
            result = Opus.INSTANCE.opus_decode(pointer, data, data.length, buffer, AudioCapture.getFrameSize(), 0);
        }

        if (result < 0) {
            VoiceClient.LOGGER.error("Failed to decode opus data: {}", result);
            return new short[0];
        }

        short[] audio = new short[result];
        buffer.get(audio);
        return audio;
    }

    public void reset() {
        if (isOpen()) {
            Opus.INSTANCE.opus_decoder_ctl(pointer, Opus.INSTANCE.OPUS_RESET_STATE);
        }
    }

    public void close() {
        if(!isOpen()) {
            VoiceClient.LOGGER.error("Opus decoder already closed");
            return;
        }

        VoiceClient.LOGGER.debug("Destroy opus decoder");
        Opus.INSTANCE.opus_decoder_destroy(pointer);
        pointer = null;
    }

    public boolean isOpen() {
        return pointer != null;
    }
}