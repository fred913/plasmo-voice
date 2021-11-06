package su.plo.voice.api.event;

public interface EventCancellable {
    void setCancelled(boolean cancelled);
    boolean isCancelled();
}
