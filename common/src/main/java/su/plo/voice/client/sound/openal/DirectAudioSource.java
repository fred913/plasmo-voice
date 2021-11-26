package su.plo.voice.client.sound.openal;

import net.minecraft.world.phys.Vec3;
import su.plo.voice.client.VoiceClient;
import su.plo.voice.protocol.packets.udp.AudioRawS2CPacket;

public class DirectAudioSource extends AbstractAudioSource {
    public DirectAudioSource(int sourceId) {
        super(sourceId);
        this.start();
    }

    @Override
    protected void createSource() {
        this.source = VoiceClient.getSoundEngine().createSource();
        this.source.setPitch(1.0F);
        this.source.setRelative(true);
        this.source.setPosition(new Vec3(0, 0, 0));
        AlUtil.checkErrors("Create custom source");
    }

    @Override
    protected boolean process(AudioRawS2CPacket pkt) {
        source.setVolume((float) VoiceClient.getClientConfig().getPlayerVolume(null, false));
        return true;
    }
}
