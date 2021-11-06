package su.plo.voice.protocol.packets.tcp;

import su.plo.voice.protocol.packets.PacketListener;

public interface ClientTcpPacketListener extends PacketListener {
    void handle(AudioEndSourceS2CPacket packet);
    void handle(AudioStatsS2CPacket packet);
    void handle(ClientConnectedS2CPacket packet);
    void handle(ClientDisconnectedS2CPacket packet);
    void handle(ClientMutedS2CPacket packet);
    void handle(ClientsListS2CPacket packet);
    void handle(ClientUnmutedS2CPacket packet);
    void handle(ConfigS2CPacket packet);
    void handle(ConnectS2CPacket packet);
    void handle(PermissionsS2CPacket packet);
    void handle(SourceInfoS2CPacket packet);
}
