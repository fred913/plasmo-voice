package su.plo.voice.server.socket;

import su.plo.voice.api.player.VoicePlayer;
import su.plo.voice.api.sources.PlayerAudioSource;
import su.plo.voice.protocol.packets.udp.AuthC2SPacket;
import su.plo.voice.protocol.packets.udp.AuthS2CPacket;
import su.plo.voice.protocol.packets.udp.MessageUdp;
import su.plo.voice.protocol.packets.udp.PingS2CPacket;
import su.plo.voice.server.VoiceServer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class SocketServerUDPQueue extends Thread {
    public LinkedBlockingQueue<MessageUdp> queue = new LinkedBlockingQueue<>();

    public void run() {
        while (!this.isInterrupted()) {
            try {
                this.keepAlive();

                MessageUdp message = queue.poll(10, TimeUnit.MILLISECONDS);
                if (message == null || message.getPayload() == null || System.currentTimeMillis() - message.getTimestamp() > message.getTtl()) {
                    continue;
                }

                if (message.getPayload() instanceof AuthC2SPacket packet) {
                    handleAuth(message, packet);
                    continue;
                }

                SocketClientUDP client = SocketServerUDP.getSender(message);
                if (client == null) {
                    continue;
                }

                message.getPayload().handle(client);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException ignored) {
                break;
            }
        }
    }

    private void keepAlive() throws IOException {
        long timestamp = System.currentTimeMillis();
        PingS2CPacket keepAlive = new PingS2CPacket();
        List<SocketClientUDP> timedOut = new ArrayList<>(SocketServerUDP.clients.size());
        for (SocketClientUDP connection : SocketServerUDP.clients.values()) {
            if (timestamp - connection.getKeepAlive() >= 15000L) {
                timedOut.add(connection);
            } else if (timestamp - connection.getSentKeepAlive() >= 1000L) {
                connection.setSentKeepAlive(timestamp);
                SocketServerUDP.sendTo(MessageUdp.write(keepAlive, null, 0L), connection);
            }
        }
        for (SocketClientUDP client : timedOut) {
            VoicePlayer player = client.getPlayer();
            client.close();

            if (VoiceServer.isLogsEnabled()) {
                VoiceServer.LOGGER.info("{} UDP timed out", player.getName());
                VoiceServer.LOGGER.info("{} sent reconnect packet", player.getName());
            }

            VoiceServer.getNetwork().reconnectClient(player);
        }
    }

    public void handleAuth(MessageUdp message, AuthC2SPacket packet) {
        UUID playerId = VoiceServer.getNetwork().findByToken(packet.getToken());

        if (playerId != null) {
            // register player source
            PlayerAudioSource source = VoiceServer.getAPI().getSourceManager().registerPlayer(playerId);

            VoicePlayer player = VoiceServer.getAPI().getPlayerManager().getByUniqueId(playerId);

            player.setId(source.getId());
            player.setModLoader(VoiceServer.getAPI().getPlayerManager().isVanillaPlayer(playerId)  ? "forge" : "fabric");

            SocketClientUDP sock = new SocketClientUDP(player, source, message.getAddress());

            if (!SocketServerUDP.clients.containsKey(player.getUniqueId())) {
                SocketServerUDP.clients.put(player.getUniqueId(), sock);
            }

            SocketServerUDP.sendTo(MessageUdp.write(new AuthS2CPacket(), null, 0L), sock);
        }
    }
}
