package su.plo.voice.server.socket;

import su.plo.voice.api.player.VoicePlayer;
import su.plo.voice.protocol.packets.Packet;
import su.plo.voice.protocol.packets.udp.AbstractPacketUdp;
import su.plo.voice.protocol.packets.udp.MessageUdp;
import su.plo.voice.server.VoiceServer;

import java.io.IOException;
import java.net.*;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SocketServerUDP extends Thread {
    public static final ConcurrentHashMap<UUID, SocketClientUDP> clients = new ConcurrentHashMap<>();
    private final SocketAddress addr;

    public static boolean started;
    private final SocketServerUDPQueue queue;
    private static DatagramSocket socket;

    public SocketServerUDP(String ip, int port) {
        this.addr = new InetSocketAddress(ip, port);
        this.queue = new SocketServerUDPQueue();
        this.queue.start();
    }

    public static void sendToNearbyPlayers(AbstractPacketUdp packet,
                                           Long timestamp,
                                           long sequenceNumber, VoicePlayer player, double maxDistance) {
        double maxDistanceSquared = maxDistance * maxDistance * 1.25F;

        byte[] bytes = MessageUdp.write((Packet) packet, timestamp, sequenceNumber);
        SocketServerUDP.clients.forEach((uuid, sock) -> {
//            if (!player.getUniqueId().equals(uuid)) {
//                VoicePlayer p = VoiceServer.getAPI().getPlayerManager().getByUniqueId(uuid);
//
//                if (maxDistanceSquared > 0) {
//                    if (!player.inRadius(p, maxDistanceSquared)) {
//                        return;
//                    }
//                }
//
//                SocketServerUDP.sendTo(bytes, sock);
//            }
            SocketServerUDP.sendTo(bytes, sock);
        });
    }

    public static SocketClientUDP getSender(MessageUdp packet) {
        return clients.values().stream()
                .filter(connection -> connection.getAddress().equals(packet.getAddress())) // todo check if works properly with proxy
                .findAny().orElse(null);
    }

    public static void sendTo(byte[] data, SocketClientUDP connection) {
        try {
            socket.send(new DatagramPacket(data, data.length, connection.getAddress()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        if (socket != null) {
            socket.close();
            queue.interrupt();
        }
        this.interrupt();
    }

    public void run() {
        try {
            socket = new DatagramSocket(this.addr);
            socket.setTrafficClass(0x04); // IPTOS_RELIABILITY
        } catch (SocketException e) {
            e.printStackTrace();
            VoiceServer.LOGGER.info(String.format("Failed to bind socket. Check if port %d UDP is open",
                    VoiceServer.getServerConfig().getPort()));
            return;
        }

        started = true;
        VoiceServer.LOGGER.info("Voice UDP server started on {}", addr.toString());

        while (!socket.isClosed()) {
            try {
                MessageUdp message = MessageUdp.read(socket);
                if (message == null) {
                    return;
                }

                queue.queue.offer(message);

                synchronized (queue) {
                    queue.notify();
                }
            } catch (IllegalStateException | IOException e) { // bad packet? just ignore it 4HEad
                if (VoiceServer.getInstance().getConfig().getBoolean("debug")) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        VoiceServer.LOGGER.info("Voice UDP server stopped");
    }
}
