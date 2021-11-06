package su.plo.voice.client.network;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import su.plo.voice.client.VoiceClient;
import su.plo.voice.protocol.data.VoiceClientInfo;
import su.plo.voice.protocol.packets.Packet;
import su.plo.voice.protocol.packets.tcp.MessageTcp;
import su.plo.voice.protocol.packets.tcp.SourceInfoC2SPacket;
import su.plo.voice.protocol.sources.PlayerSourceInfo;
import su.plo.voice.protocol.sources.SourceInfo;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class ClientNetworkHandler extends ClientNetworkListener {
    public ClientNetworkHandler(Connection connection) {
        super(connection);
    }

    /**
     * Check if player has voice chat
     */
    public boolean hasVoiceChat(UUID uuid) {
        return clients.containsKey(uuid);
    }

    /**
     * Check if player muted
     */
    public boolean isPlayerMuted(UUID uuid) {
        VoiceClientInfo client = clients.get(uuid);
        return client != null && client.isMuted();
    }

    /**
     * Check if player muted
     */
    public boolean isPlayerMuted(int id) {
        VoiceClientInfo client = null;
        try {
            client = VoiceClient.getNetwork().getClientById(id);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return client == null || client.isMuted();
    }

    /**
     * Check if source is talking
     */
    public Boolean getTalking(int sourceId) {
        return talking.get(sourceId);
    }

    /**
     * Check if source is talking
     */
    public Boolean getTalking(UUID uuid) {
        VoiceClientInfo client = clients.get(uuid);
        if (client != null) {
            return talking.get(client.getId());
        }

        return null;
    }

    /**
     * Get voice client info
     *
     * @param id source id
     * @return null or VoiceClientInfo
     */
    public VoiceClientInfo getClientById(int id) throws IOException {
        VoiceClientInfo client = clientsById.get(id);
        if (client == null) {
            PlayerSourceInfo sourceInfo = (PlayerSourceInfo) getSourceInfo(id);
            if (sourceInfo == null) {
                return null;
            }

            client = sourceInfo.getClient();
            clients.put(client.getUuid(), client);
            clientsById.put(client.getId(), client);
        }

        return client;
    }

    /**
     * Get source info
     *
     * @return null or VoiceClientInfo
     */
    public SourceInfo getSourceInfo(int sourceId) throws IOException {
        if (sourcesById.containsKey(sourceId)) {
            return sourcesById.get(sourceId);
        }

        CompletableFuture<SourceInfo> sourceInfo;
        if (sourceRequests.containsKey(sourceId)) {
            sourceInfo = sourceRequests.get(sourceId);
        } else {
            sourceInfo = new CompletableFuture<>();
            sourceRequests.put(sourceId, sourceInfo);

            sendToServer(new SourceInfoC2SPacket(sourceId));
        }

        try {
            return sourceInfo.get(1000L, TimeUnit.MILLISECONDS);
        } catch (Exception ignored) {
        }

        return null;
    }

    public void handle(FriendlyByteBuf buf) {
        byte[] data = new byte[buf.readableBytes()];
        buf.duplicate().readBytes(data);
        ByteArrayDataInput in = ByteStreams.newDataInput(data);

        Packet pkt = MessageTcp.read(in);
        if (pkt != null) {
            pkt.handle(this);
        }
    }
}
