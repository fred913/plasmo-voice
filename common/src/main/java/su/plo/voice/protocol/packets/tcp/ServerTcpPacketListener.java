package su.plo.voice.protocol.packets.tcp;

import su.plo.voice.protocol.packets.PacketListener;

public interface ServerTcpPacketListener extends PacketListener {
    void handle(AudioEndPlayerC2SPacket packet);
    void handle(AudioResetStatsC2SPacket packet);
    void handle(ConnectC2SPacket packet);
    void handle(SourceInfoC2SPacket packet);
}
