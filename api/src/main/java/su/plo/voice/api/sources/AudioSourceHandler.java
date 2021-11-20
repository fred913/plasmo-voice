package su.plo.voice.api.sources;

public interface AudioSourceHandler {
    boolean canProvide();

    byte[] provide20MsAudio();

    boolean isOpusEncoded();

    int getDistance();
}
