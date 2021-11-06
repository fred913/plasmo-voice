package su.plo.voice.client.socket;

import su.plo.voice.client.VoiceClient;

public class SocketClientPing extends Thread {
    private final SocketClientUDP socketUDP;
    public boolean timedOut = false;

    public SocketClientPing(SocketClientUDP socketUDP) {
        this.socketUDP = socketUDP;
    }

    @Override
    public void run() {
        VoiceClient.LOGGER.info("Start ping");

        while(!socketUDP.isClosed()) {
            try {
                socketUDP.checkTimeout();
                Thread.sleep(1000L);
            } catch (InterruptedException ignored) {}
        }
    }
}
