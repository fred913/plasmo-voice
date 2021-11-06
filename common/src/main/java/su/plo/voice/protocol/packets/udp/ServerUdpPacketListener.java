package su.plo.voice.protocol.packets.udp;

import su.plo.voice.protocol.packets.PacketListener;

public interface ServerUdpPacketListener extends PacketListener {
    void handle(AudioDirectC2SPacket packet);
    void handle(AudioPlayerC2SPacket packet);
    void handle(AudioRawC2SPacket packet);
//    void handle(AuthC2SPacket packet);
    void handle(PingC2SPacket packet);
}
