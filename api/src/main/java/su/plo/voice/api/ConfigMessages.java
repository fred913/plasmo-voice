package su.plo.voice.api;

import javax.annotation.Nullable;

public interface ConfigMessages {
    @Nullable
    String getMessagePrefix(String name);

    @Nullable
    String getMessage(String name);

    @Nullable
    String getPrefix();
}
