package su.plo.voice.protocol.packets.udp;

import su.plo.voice.protocol.packets.PacketListener;

public interface ClientUdpPacketListener extends PacketListener {
    void handle(AudioDirectS2CPacket packet);
    void handle(AudioPlayerS2CPacket packet);
    void handle(AudioRawS2CPacket packet);
    void handle(AudioSourceS2CPacket packet);
    void handle(AuthS2CPacket packet);
    void handle(PingS2CPacket packet);
}
