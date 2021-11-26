package su.plo.voice.client.sound.openal;

import net.minecraft.world.entity.player.Player;
import su.plo.voice.client.VoiceClient;
import su.plo.voice.protocol.data.VoiceClientInfo;
import su.plo.voice.protocol.packets.udp.AudioPlayerS2CPacket;
import su.plo.voice.protocol.packets.udp.AudioRawS2CPacket;

public class PlayerAudioSource extends AbstractAudioSource {
    private final VoiceClientInfo client;

    public PlayerAudioSource(int sourceId, VoiceClientInfo client) {
        super(sourceId);
        this.client = client;
        this.start();
    }

    @Override
    protected boolean process(AudioRawS2CPacket pkt) {
        AudioPlayerS2CPacket packet = (AudioPlayerS2CPacket) pkt;

        Player player = minecraft.level.getPlayerByUUID(client.getUuid());
        if(player == null) {
            return false;
        }

        final Player localPlayer = minecraft.player;

        boolean isPriority = packet.getDistance() > VoiceClient.getServerConfig().getMaxDistance();
        float distance = (float) player.position().distanceTo(localPlayer.position());
        float percentage = (float) VoiceClient.getClientConfig().getPlayerVolume(player.getUUID(), isPriority);

        if (percentage == 0.0F) {
            return false;
        }

        percentage *= (float) (1D - lastOcclusion);

        int maxDistance = packet.getDistance();
        if(distance > maxDistance) {
            sequenceNumber = -1L;
            lastOcclusion = -1;
            return false;
        }

        int fadeDistance = isPriority
                ? maxDistance / VoiceClient.getServerConfig().getPriorityFadeDivisor()
                : maxDistance / VoiceClient.getServerConfig().getFadeDivisor();

        if(isSoundOcclusion()) {
            percentage *= (float) (1D - calculateOcclusion(localPlayer, player.position()));
        }

        source.setPosition(player.position());
        source.setDirection(player.getLookAngle());
        source.setVolume(percentage);

        source.setFadeDistance(fadeDistance);
        source.setMaxDistance(maxDistance, 0.95F);

        return true;
    }
}
