package su.plo.voice.client.sound.activation;

import su.plo.voice.client.VoiceClient;

public class ActivationPTT implements Activation {
    private long lastSpeak;

    @Override
    public Result process(short[] samples) {
        boolean priorityPressed = VoiceClient.getClientConfig().keyBindings.priorityPushToTalk.get().isPressed()
                && VoiceClient.getServerConfig().isPriority()
                && VoiceClient.getServerConfig().getPriorityDistance() > VoiceClient.getServerConfig().getMaxDistance();
        boolean pushToTalkPressed = VoiceClient.getClientConfig().keyBindings.pushToTalk.get().isPressed()
                || priorityPressed;

        if (!VoiceClient.isMicrophoneLoopback()) {
            if (!VoiceClient.isSpeakingPriority() && priorityPressed) {
                VoiceClient.setSpeakingPriority(true);
            } else if (VoiceClient.isSpeaking() && VoiceClient.isSpeakingPriority() && !priorityPressed && pushToTalkPressed) {
                VoiceClient.setSpeakingPriority(false);
            }
        }

        if (pushToTalkPressed && !VoiceClient.isSpeaking() && !VoiceClient.isMicrophoneLoopback()) {
            VoiceClient.setSpeaking(true);
            this.lastSpeak = System.currentTimeMillis();
        } else if (pushToTalkPressed && !VoiceClient.isMicrophoneLoopback()) {
            this.lastSpeak = System.currentTimeMillis();
        } else if (VoiceClient.isSpeaking() && (System.currentTimeMillis() - lastSpeak > 350L || VoiceClient.isMicrophoneLoopback())) {
            VoiceClient.setSpeaking(false);
            VoiceClient.setSpeakingPriority(false);

            return Result.End;
        }

        if (!VoiceClient.isSpeaking()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ignored) {
            }
            return Result.NotDetected;
        }

        return Result.Process;
    }
}
