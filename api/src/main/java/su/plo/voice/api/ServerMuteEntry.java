package su.plo.voice.api;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class ServerMuteEntry {
    private final UUID uuid;
    private final Long to;
    private final String reason;
}
