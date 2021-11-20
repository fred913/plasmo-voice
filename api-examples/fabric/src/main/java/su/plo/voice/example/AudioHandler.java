package su.plo.voice.example;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame;
import su.plo.voice.api.sources.AudioSourceHandler;

import java.nio.Buffer;
import java.nio.ByteBuffer;

public class AudioHandler implements AudioSourceHandler {
    private final AudioPlayer player;
    private final ByteBuffer buffer = ByteBuffer.allocate(1024);
    private final MutableAudioFrame frame = new MutableAudioFrame();

    public AudioHandler(AudioPlayer player) {
        this.player = player;
        frame.setBuffer(buffer);
    }

    @Override
    public boolean canProvide() {
        return player.provide(frame);
    }

    @Override
    public byte[] provide20MsAudio() {
        ((Buffer) buffer).flip();

        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);

        return bytes;
    }

    @Override
    public boolean isOpusEncoded() {
        return true;
    }

    @Override
    public int getDistance() {
        return 16;
    }
}