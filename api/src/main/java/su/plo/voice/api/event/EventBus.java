package su.plo.voice.api.event;

public interface EventBus {
    /**
     * Register event listener
     */
    void registerEvents(Listener listener);

    /**
     * Unregister event listener
     */
    void unregisterEvents(Listener listener);
}
