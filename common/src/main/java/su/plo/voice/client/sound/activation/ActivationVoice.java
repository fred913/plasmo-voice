package su.plo.voice.client.sound.activation;

import su.plo.voice.client.VoiceClient;
import su.plo.voice.utils.AudioUtils;

public class ActivationVoice implements Activation {
    private long lastSpeak;

    @Override
    public Result process(short[] samples) {
        boolean priorityPressed = VoiceClient.getClientConfig().keyBindings.pushToTalk.get().isPressed()
                && VoiceClient.getServerConfig().isPriority()
                && VoiceClient.getServerConfig().getPriorityDistance() > VoiceClient.getServerConfig().getMaxDistance();

        if (VoiceClient.isMicrophoneLoopback()) {
            if (VoiceClient.isSpeaking()) {
                VoiceClient.setSpeaking(false);
                VoiceClient.setSpeakingPriority(false);
            }
            return Result.NotDetected;
        }

        if (!VoiceClient.isSpeakingPriority() && priorityPressed) {
            VoiceClient.setSpeakingPriority(true);
        } else if (VoiceClient.isSpeaking() && VoiceClient.isSpeakingPriority() && !priorityPressed) {
            VoiceClient.setSpeakingPriority(false);
        }

        boolean wasActivated = System.currentTimeMillis() - lastSpeak <= 500L;
        boolean isActivated = AudioUtils.hasHigherLevel(samples, VoiceClient.getClientConfig().voiceActivationThreshold.get());
        if (isActivated || wasActivated) {
            if (isActivated) {
                this.lastSpeak = System.currentTimeMillis();
            }

            if (!VoiceClient.isSpeaking()) {
                VoiceClient.setSpeaking(true);
//                if (this.lastBuffer != null) {
//                    this.send(lastBuffer);
//                }
            }
        } else if (VoiceClient.isSpeaking()) {
            VoiceClient.setSpeaking(false);
            VoiceClient.setSpeakingPriority(false);

            return Result.End;
        }

        return VoiceClient.isSpeaking() ? Result.Process : Result.NotDetected;
    }
}
