package su.plo.voice.example;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import lombok.Getter;
import net.fabricmc.api.ModInitializer;
import su.plo.voice.api.PlasmoVoiceAPI;
import su.plo.voice.api.PlasmoVoiceProvider;

import java.util.ArrayList;
import java.util.List;

public class PVoiceApiExample implements ModInitializer {
    @Getter
    private static AudioPlayerManager lavaManager = new DefaultAudioPlayerManager();
    @Getter
    private static List<AudioPlayer> players = new ArrayList<>();

    public static PlasmoVoiceAPI getAPI() {
        return PlasmoVoiceProvider.getAPI();
    }

    @Override
    public void onInitialize() {
        AudioSourceManagers.registerRemoteSources(lavaManager);
        lavaManager.registerSourceManager(new YoutubeAudioSourceManager());

        // api test
//        for (int i = 0; i < 1; i++) {
//            StaticAudioSource source = API.getSourceManager().registerStatic("New World", new Pos3d(i * 3, 80, 0));
//
//            AudioPlayer player = lavaManager.createPlayer();
//            source.setSourceHandler(new PepegaHandler(player));
//            AudioEventListener audioListener = new AudioEventAdapter() {
//                @Override
//                public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
//                    source.sendEnd(16);
//                    track.setPosition(0L);
//                    player.playTrack(track.makeClone());
//                }
//            };
//
//            player.addListener(audioListener);
//            players.add(player);
//
//            lavaManager.loadItem("dQw4w9WgXcQ", new AudioLoadResultHandler() {
//                @Override
//                public void trackLoaded(AudioTrack track) {
//                    player.playTrack(track);
//                }
//
//                @Override
//                public void playlistLoaded(AudioPlaylist playlist) {
//
//                }
//
//                @Override
//                public void noMatches() {
//
//                }
//
//                @Override
//                public void loadFailed(FriendlyException exception) {
//
//                }
//            });
//        }
    }
}
