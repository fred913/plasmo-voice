package su.plo.voice.api.event.player;

import lombok.Getter;
import su.plo.voice.api.ServerMuteEntry;
import su.plo.voice.api.event.EventCancellable;
import su.plo.voice.api.event.VoiceEvent;
import su.plo.voice.api.player.VoicePlayer;

public class VoiceUnmuteEvent implements VoiceEvent, EventCancellable {
    @Getter
    private final VoicePlayer player;
    @Getter
    private final ServerMuteEntry mute;

    public VoiceUnmuteEvent(VoicePlayer player, ServerMuteEntry mute) {
        this.player = player;
        this.mute = mute;
    }

    private boolean cancelled;

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }
}
