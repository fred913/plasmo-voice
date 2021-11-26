package su.plo.voice.server.network;

import lombok.Setter;
import su.plo.voice.api.player.VoicePlayer;
import su.plo.voice.protocol.data.VoiceClientInfo;
import su.plo.voice.protocol.packets.tcp.*;
import su.plo.voice.protocol.sources.SourceInfo;
import su.plo.voice.server.SourceManagerImpl;
import su.plo.voice.server.VoiceServer;
import su.plo.voice.server.config.ServerConfig;
import su.plo.voice.server.socket.SocketServerUDP;

import java.util.ArrayList;
import java.util.List;

public class ServerNetworkListener implements ServerTcpPacketListener {
    @Setter
    private VoicePlayer player;

    public ServerNetworkListener(VoicePlayer player) {
        this.player = player;
    }

    @Override
    public void handle(AudioEndPlayerC2SPacket packet) {
        VoiceServer.getNetwork().sendToNearbyClients(
                new AudioEndSourceS2CPacket(player.getId(), packet.getSequenceNumber()),
                player,
                packet.getDistance()
        );
    }

    @Override
    public void handle(AudioResetStatsC2SPacket audioResetStatsC2SPacket) {

    }

    @Override
    public void handle(ConnectC2SPacket packet) {
        ServerConfig config = VoiceServer.getServerConfig();

        int[] version = VoiceServer.calculateVersion(packet.getVersion());

        if (version[0] > VoiceServer.getInstance().getVersion()[0]) {
            player.sendTranslatableMessage(
                    "message.plasmo_voice.version_not_supported",
                    String.format("%d.X.X", VoiceServer.getInstance().getVersion()[0])
            );
            return;
        } else if (version[0] < VoiceServer.getInstance().getVersion()[0]) {
            player.sendTranslatableMessage(
                    "message.plasmo_voice.min_version",
                    String.format("%d.X.X", VoiceServer.getInstance().getVersion()[0])
            );
            return;
        }
        // todo client-sided
//        else if (ver < VoiceServer.version) {
//            TextComponent link = new TextComponent("https://www.curseforge.com/minecraft/mc-mods/plasmo-voice-client/files");
//            link.setStyle(link.getStyle().withClickEvent(
//                    new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.curseforge.com/minecraft/mc-mods/plasmo-voice-client/files")
//            ));
//
//            player.sendMessage(
//                    new TranslatableComponent(
//                            "message.plasmo_voice.new_version_available",
//                            VoiceServer.rawVersion,
//                            link
//                    ),
//                    VoiceServer.NIL_UUID
//            );
//        }

        VoiceServer.getNetwork().sendTo(
                player,
                new ConfigS2CPacket(
                        config.getSampleRate(),
                        new ArrayList<>(config.getDistances()),
                        config.getDefaultDistance(),
                        config.getMaxPriorityDistance(),
                        config.getFadeDivisor(),
                        config.getPriorityFadeDivisor(),

                        player.hasPermission("voice.priority"),
                        player.hasPermission("voice.speak"),
                        player.hasPermission("voice.activation"),
                        player.hasPermission("voice.force_sound_occlusion"),
                        !player.hasPermission("voice.icons"),
                        player.getForceDistance(),
                        player.getForcePriorityDistance()
                )
        );

        List<VoiceClientInfo> clients = new ArrayList<>();
        SocketServerUDP.clients.forEach((uuid, c) -> {
            clients.add(new VoiceClientInfo(c.getPlayer().getId(), uuid, VoiceServer.getAPI().isMuted(uuid)));
        });

        VoiceServer.getNetwork().sendTo(
                player,
                new ClientsListS2CPacket(clients)
        );

        VoiceServer.getNetwork().sendToClients(
                new ClientConnectedS2CPacket(new VoiceClientInfo(player.getId(), player.getUniqueId(), VoiceServer.getAPI().isMuted(player.getUniqueId()))),
                player
        );

        if (!VoiceServer.getInstance().getConfig().getBoolean("disable_logs")) {
            VoiceServer.LOGGER.info(String.format("New client: %s v%s", player.getName(), packet.getVersion()));
        }
    }

    @Override
    public void handle(SourceInfoC2SPacket packet) {
        SourceInfo source = ((SourceManagerImpl) VoiceServer.getAPI().getSourceManager())
                .getInfo(packet.getId());
        if (source != null) {
            VoiceServer.getNetwork().sendTo(player, new SourceInfoS2CPacket(source));
        }
    }
}
