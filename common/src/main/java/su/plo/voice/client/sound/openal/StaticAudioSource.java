package su.plo.voice.client.sound.openal;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import su.plo.voice.client.VoiceClient;
import su.plo.voice.client.sound.AbstractAudioSource;
import su.plo.voice.protocol.packets.udp.AudioRawS2CPacket;
import su.plo.voice.protocol.packets.udp.AudioSourceS2CPacket;
import su.plo.voice.protocol.sources.StaticSourceInfo;

public class StaticAudioSource extends AbstractAudioSource {
    private final StaticSourceInfo info;

    public StaticAudioSource(int sourceId, StaticSourceInfo info) {
        super(sourceId);
        this.info = info;
        this.start();
    }

    @Override
    protected boolean process(AudioRawS2CPacket pkt) {
        AudioSourceS2CPacket packet = (AudioSourceS2CPacket) pkt;

        final Vec3 position = new Vec3(info.getPosition().getX(), info.getPosition().getY(), info.getPosition().getZ());
        Vec3 lookAngle = null;
        if (info.getDirection() != null) {
            lookAngle = new Vec3(info.getDirection().getX(), info.getDirection().getY(), info.getPosition().getZ());
        }
        if (info.getAngle() != 360) {
            source.setAngle(info.getAngle());
        }

        final Player localPlayer = minecraft.player;

        boolean isPriority = packet.getDistance() > VoiceClient.getServerConfig().getMaxDistance();
        float distance = (float) position.distanceTo(localPlayer.position());
        float percentage = (float) VoiceClient.getClientConfig().getPlayerVolume(null, isPriority);

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
            percentage *= (float) (1D - calculateOcclusion(localPlayer, position));
        }

        source.setPosition(position);
        if (lookAngle != null) {
            source.setDirection(lookAngle);
        }
        source.setVolume(percentage);

        source.setFadeDistance(fadeDistance);
        source.setMaxDistance(maxDistance, 0.95F);

        return true;
    }
}
