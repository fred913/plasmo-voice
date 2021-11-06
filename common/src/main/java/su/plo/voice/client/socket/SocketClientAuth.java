package su.plo.voice.client.socket;

import su.plo.voice.client.VoiceClient;
import su.plo.voice.protocol.packets.udp.AuthC2SPacket;

public class SocketClientAuth extends Thread {
    private final SocketClientUDP socket;

    public SocketClientAuth(SocketClientUDP socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        while(!socket.authorized && !socket.isClosed()) {
            try {
                socket.send(new AuthC2SPacket(VoiceClient.getServerConfig().getSecret()), 0L);

                Thread.sleep(2000);
            } catch (InterruptedException ignored) {
                break;
            }
        }
    }
}
