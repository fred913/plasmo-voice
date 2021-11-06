package su.plo.voice.server;

import com.google.common.eventbus.AsyncEventBus;
import su.plo.voice.api.event.EventBus;
import su.plo.voice.api.event.Listener;
import su.plo.voice.api.event.VoiceEvent;

import java.util.concurrent.Executors;

public class EventBusImpl implements EventBus {
    private final AsyncEventBus eventBus = new AsyncEventBus(Executors.newSingleThreadExecutor());

    @Override
    public void registerEvents(Listener listener) {
        eventBus.register(listener);
    }

    @Override
    public void unregisterEvents(Listener listener) {
        eventBus.unregister(listener);
    }

    public void post(VoiceEvent event) {
        eventBus.post(event);
    }
}
