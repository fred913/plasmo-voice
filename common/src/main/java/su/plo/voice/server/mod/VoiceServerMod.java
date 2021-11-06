package su.plo.voice.server.mod;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import su.plo.voice.api.ServerMuteEntry;
import su.plo.voice.server.VoiceServer;
import su.plo.voice.server.VoiceServerAPIImpl;
import su.plo.voice.server.mod.config.ServerDataMod;
import su.plo.voice.server.mod.metrics.Metrics;
import su.plo.voice.server.mod.player.PlayerManagerMod;
import su.plo.voice.server.network.ServerNetworkHandler;
import su.plo.voice.server.socket.SocketServerUDP;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public abstract class VoiceServerMod extends VoiceServer {
    public static final ResourceLocation PLASMO_VOICE = new ResourceLocation("plasmo:voice");
    public static final UUID NIL_UUID = new UUID(0, 0);

    @Getter
    @Setter
    private static MinecraftServer server;

    private final Executor executor = Executors.newSingleThreadExecutor();
    private Metrics metrics;

    public VoiceServerMod(ServerNetworkHandler network) {
        super(new PlayerManagerMod(), network);
    }

    @Override
    protected void start() {
        network.start();
        super.start();
        API = new VoiceServerAPIImpl(playerManager);
    }

    @Override
    protected void close() {
        super.close();

        if (metrics != null) {
            metrics.close();
        }
    }

    protected void setupMetrics(String software) {
        metrics = new Metrics(10928, software.toLowerCase(), server);
        metrics.addCustomChart(new Metrics.SingleLineChart("players_with_forge_mod", () ->
                (int) SocketServerUDP.clients.values().stream().filter(s -> s.getPlayer().getModLoader().equals("forge")).count()));
        metrics.addCustomChart(new Metrics.SingleLineChart("players_with_fabric_mod", () ->
                (int) SocketServerUDP.clients.values().stream().filter(s -> s.getPlayer().getModLoader().equals("fabric")).count()));

        metrics.addCustomChart(new Metrics.SimplePie("server_type", () -> software));
    }

    @Override
    public int getPort() {
        return server.getPort();
    }

    @Override
    public File getDataFolder() {
        return new File("config/PlasmoVoice");
    }

    @Override
    public void loadData() {
        ServerDataMod serverData = ServerDataMod.read(getDataFolder());
        if (serverData != null) {
            for (ServerMuteEntry e : serverData.getMuted()) {
                VoiceServer.getAPI().getMutedMap().put(e.getUuid(), e);
            }

            ((PlayerManagerMod) playerManager).getPermissions().putAll(serverData.getPermissions());
        }
    }

    @Override
    public void saveData(boolean async) {
        if (async) {
            ServerDataMod.saveAsync(
                    getDataFolder(),
                            new ServerDataMod(new ArrayList<>(API.getMutedMap().values()), ((PlayerManagerMod) playerManager).getPermissions())
                    );
        } else {
            ServerDataMod.save(
                    getDataFolder(),
                    new ServerDataMod(new ArrayList<>(API.getMutedMap().values()), ((PlayerManagerMod) playerManager).getPermissions())
            );
        }
    }

    @Override
    public void runAsync(Runnable runnable) {
        executor.execute(runnable);
    }
}
