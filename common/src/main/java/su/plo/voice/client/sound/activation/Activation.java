package su.plo.voice.client.sound.activation;

public interface Activation {
    Result process(short[] samples);

    enum Result {
        NotDetected,
        Process,
        End
    }

    enum Type {
        PushToTalk,
        Voice
    }
}
