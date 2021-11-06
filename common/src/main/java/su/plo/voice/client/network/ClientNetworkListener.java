package su.plo.voice.client.network;

import io.netty.buffer.Unpooled;
import io.netty.channel.local.LocalAddress;
import lombok.Getter;
import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import su.plo.voice.client.VoiceClient;
import su.plo.voice.client.config.ServerSettings;
import su.plo.voice.client.gui.VoiceNotAvailableScreen;
import su.plo.voice.client.socket.SocketClientUDP;
import su.plo.voice.client.socket.SocketClientUDPListener;
import su.plo.voice.client.sound.AbstractSoundQueue;
import su.plo.voice.protocol.data.VoiceClientInfo;
import su.plo.voice.protocol.packets.Packet;
import su.plo.voice.protocol.packets.tcp.*;
import su.plo.voice.protocol.sources.PlayerSourceInfo;
import su.plo.voice.protocol.sources.SourceInfo;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class ClientNetworkListener implements ClientTcpPacketListener {
    protected final Minecraft client = Minecraft.getInstance();
    protected final Connection connection;

    // clients
    @Getter
    protected final Map<UUID, VoiceClientInfo> clients = new ConcurrentHashMap<>();
    protected final Map<Integer, VoiceClientInfo> clientsById = new ConcurrentHashMap<>();

    // sources
    protected final Map<Integer, SourceInfo> sourcesById = new ConcurrentHashMap<>();
    protected final Map<Integer, CompletableFuture<SourceInfo>> sourceRequests = new ConcurrentHashMap<>();
    @Getter
    protected final ConcurrentHashMap<Integer, Boolean> talking = new ConcurrentHashMap<>();

    public ClientNetworkListener(Connection connection) {
        this.connection = connection;
    }

    /**
     * Send packet to server
     * @param packet C2S (Client-to-Server) packet
     */
    public void sendToServer(Packet<ServerTcpPacketListener> packet) throws IOException {
        connection.send(
                new ServerboundCustomPayloadPacket(
                        VoiceClient.PLASMO_VOICE,
                        new FriendlyByteBuf(Unpooled.wrappedBuffer(MessageTcp.write(packet)))
                )
        );
    }


    /* Connection packet */
    @SneakyThrows
    @Override
    public void handle(ConnectS2CPacket packet) {
        VoiceClient.disconnect();
        VoiceClient.socketUDP = null;

        if (!(connection.getRemoteAddress() instanceof InetSocketAddress) &&
                !(connection.getRemoteAddress() instanceof LocalAddress)) {
            return;
        }

        String ip = packet.getIp();
        String serverIp = "127.0.0.1";

        if (ip.equals("0.0.0.0")) {
            if (connection.getRemoteAddress() instanceof InetSocketAddress addr) {
                Inet4Address in4addr = (Inet4Address) addr.getAddress();
                String[] ipSplit = in4addr.toString().split("/");

                serverIp = ipSplit[0];
                if (ipSplit.length > 1) {
                    serverIp = ipSplit[1];
                }
            }

            ip = serverIp;
        }

        VoiceClient.LOGGER.info("UDP server address: {}:{}", ip, packet.getPort());
        VoiceClient.LOGGER.info("Connecting to " + (client.getCurrentServer() == null ? "localhost" : client.getCurrentServer().ip));

        VoiceClient.setServerConfig(new ServerSettings(packet.getToken(), ip, packet.getPort()));

        this.sendToServer(new ConnectC2SPacket(packet.getToken(), VoiceClient.getInstance().getVersion()));
    }
    /* Connection packet */


    /* Source info response */
    @Override
    public void handle(SourceInfoS2CPacket packet) {
        SourceInfo info = packet.getInfo();

        CompletableFuture<SourceInfo> request = sourceRequests.get(info.getId());
        if (request != null) {
            request.complete(info);
        }

        if (!(info instanceof PlayerSourceInfo)) {
            sourcesById.put(info.getId(), info);
        }
    }
    /* Source info response */


    /* Server config */
    @SneakyThrows
    public void handle(ConfigS2CPacket packet) {
        if (VoiceClient.getServerConfig() != null) {
            VoiceClient.getServerConfig().update(packet);

            if (!VoiceClient.isConnected()) {
                VoiceClient.socketUDP = new SocketClientUDP();
                VoiceClient.socketUDP.start();

                if (client.screen instanceof VoiceNotAvailableScreen screen) {
                    screen.setConnecting();
                }
            }
        }
    }

    @Override
    public void handle(PermissionsS2CPacket packet) {

        if (VoiceClient.getServerConfig() != null) {
            VoiceClient.getServerConfig().update(packet);
        }
    }
    /* Server config */


    /* Clients info events (mutes/connections) */
    @Override
    public void handle(ClientConnectedS2CPacket packet) {
        if (VoiceClient.isConnected()) {
            clients.put(packet.getClient().getUuid(), packet.getClient());
            clientsById.put(packet.getClient().getId(), packet.getClient());
        }
    }

    @Override
    public void handle(ClientDisconnectedS2CPacket packet) {
        if (VoiceClient.isConnected()) {
            VoiceClientInfo client = clients.get(packet.getClient());
            if (client != null) {
                clients.remove(packet.getClient());
                clientsById.remove(client.getId());
            }
        }
    }

    @Override
    public void handle(ClientMutedS2CPacket packet) {
        if (VoiceClient.isConnected() && clients.containsKey(packet.getClient())) {
            VoiceClientInfo client = clients.get(packet.getClient());
            client.setMuted(true);

            AbstractSoundQueue queue = SocketClientUDPListener.audioChannels.get(client.getId());
            if (queue != null) {
                queue.close();
                SocketClientUDPListener.audioChannels.remove(client.getId());
                talking.remove(client.getId());
            }

            VoiceClient.LOGGER.info(packet.getClient().toString() + " muted");
        }
    }

    @Override
    public void handle(ClientUnmutedS2CPacket packet) {
        if (VoiceClient.isConnected() && clients.containsKey(packet.getClient())) {
            clients.get(packet.getClient()).setMuted(false);

            VoiceClient.LOGGER.info(packet.getClient().toString() + " unmuted");
        }
    }

    @Override
    public void handle(ClientsListS2CPacket packet) {
        if (VoiceClient.getServerConfig() != null) { // check only config, because it can be sent before udp is connected
            clients.clear();

            for (VoiceClientInfo client : packet.getClients()) {
                clients.put(client.getUuid(), client);
                clientsById.put(client.getId(), client);
            }

            VoiceClient.LOGGER.info("Clients: " + packet.getClients().toString());
        }
    }
    /* Clients info events (mutes/connections) */


    /* Audio stats */
    @Override
    public void handle(AudioEndSourceS2CPacket packet) {
        talking.remove(packet.getSourceId());

        AbstractSoundQueue ch = SocketClientUDPListener.audioChannels.get(packet.getSourceId());
        if (ch != null) {
            ch.end();
        }
    }

    @Override
    public void handle(AudioStatsS2CPacket packet) {

    }
    /* Audio stats */
}
