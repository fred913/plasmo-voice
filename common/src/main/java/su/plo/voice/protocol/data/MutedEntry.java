package su.plo.voice.protocol.data;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@AllArgsConstructor
@Data
public class MutedEntry {
    private UUID uuid;
    private Long to;
}
