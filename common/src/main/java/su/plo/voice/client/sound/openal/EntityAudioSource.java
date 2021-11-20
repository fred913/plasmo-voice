package su.plo.voice.client.sound.openal;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import su.plo.voice.client.VoiceClient;
import su.plo.voice.client.sound.AbstractAudioSource;
import su.plo.voice.protocol.packets.udp.AudioRawS2CPacket;
import su.plo.voice.protocol.packets.udp.AudioSourceS2CPacket;
import su.plo.voice.protocol.sources.EntitySourceInfo;

public class EntityAudioSource extends AbstractAudioSource {
    private final EntitySourceInfo info;

    public EntityAudioSource(int sourceId, EntitySourceInfo info) {
        super(sourceId);
        this.info = info;
        this.start();
    }

    @Override
    protected boolean process(AudioRawS2CPacket pkt) {
        AudioSourceS2CPacket packet = (AudioSourceS2CPacket) pkt;

        Entity entity = minecraft.level.getEntity(info.getEntityId());
        if(entity == null) {
            return false;
        }

        final Player localPlayer = minecraft.player;

        boolean isPriority = packet.getDistance() > VoiceClient.getServerConfig().getMaxDistance();
        float distance = (float) entity.position().distanceTo(localPlayer.position());
        float percentage = (float) VoiceClient.getClientConfig().getPlayerVolume(entity.getUUID(), isPriority);

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

        if(!VoiceClient.getSoundEngine().isSoundPhysics() && VoiceClient.getClientConfig().occlusion.get()) {
            percentage *= (float) (1D - calculateOcclusion(localPlayer, entity.position()));
        }

        source.setPosition(entity.position());
        source.setDirection(entity.getLookAngle());
        source.setVolume(percentage);

        source.setFadeDistance(fadeDistance);
        source.setMaxDistance(maxDistance, 0.95F);

        return true;
    }
}
