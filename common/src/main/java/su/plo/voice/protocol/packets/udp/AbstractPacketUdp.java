package su.plo.voice.protocol.packets.udp;

import lombok.Getter;
import lombok.Setter;

public abstract class AbstractPacketUdp {
    @Getter
    @Setter
    private MessageUdp message;
}
