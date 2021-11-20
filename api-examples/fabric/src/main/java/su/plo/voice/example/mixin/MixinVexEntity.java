package su.plo.voice.example.mixin;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventListener;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Vex;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import su.plo.voice.api.sources.EntityAudioSource;
import su.plo.voice.example.AudioHandler;
import su.plo.voice.example.PVoiceApiExample;

@Mixin(ServerEntity.class)
public class MixinVexEntity {
    @Shadow @Final private Entity entity;

    @Inject(method = "<init>*", at = @At("RETURN"))
    private void onConstructed(CallbackInfo ci) {
        if (this.entity instanceof Vex) {
            EntityAudioSource source = PVoiceApiExample.getAPI().getSourceManager().registerEntity(this.entity.getUUID());

            AudioPlayer player = PVoiceApiExample.getLavaManager().createPlayer();
            source.setSourceHandler(new AudioHandler(player));
            AudioEventListener audioListener = new AudioEventAdapter() {
                @Override
                public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
                    source.sendEnd(16);
                    track.setPosition(0L);
                    player.playTrack(track.makeClone());
                }
            };

            player.addListener(audioListener);
            PVoiceApiExample.getPlayers().add(player);

            PVoiceApiExample.getLavaManager().loadItem("XCyDXRvajyY", new AudioLoadResultHandler() {
                @Override
                public void trackLoaded(AudioTrack track) {
                    player.playTrack(track);
                }

                @Override
                public void playlistLoaded(AudioPlaylist playlist) {
                }

                @Override
                public void noMatches() {
                }

                @Override
                public void loadFailed(FriendlyException exception) {
                }
            });
        }
    }
}
