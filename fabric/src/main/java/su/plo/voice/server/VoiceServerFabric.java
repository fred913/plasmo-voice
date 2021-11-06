package su.plo.voice.server;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.S2CPlayChannelEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import su.plo.voice.server.config.ServerConfigFabric;
import su.plo.voice.server.mod.VoiceServerMod;
import su.plo.voice.server.mod.commands.CommandManager;
import su.plo.voice.server.mod.network.ServerNetworkHandlerFabric;
import su.plo.voice.server.mod.network.ServerNetworkHandlerMod;

public class VoiceServerFabric extends VoiceServerMod implements ModInitializer {
    private int[] version = new int[3];

    public VoiceServerFabric() {
        super(new ServerNetworkHandlerFabric());
    }

    @Override
    public void onInitialize() {
        loadVersion();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            setServer(server);
            this.start();

            if (server.isDedicatedServer()) {
                this.setupMetrics("Fabric");
            }
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> this.close());

        ServerNetworkHandlerMod modNetwork = (ServerNetworkHandlerMod) network;

        S2CPlayChannelEvents.REGISTER.register((handler, sender, server, channels) ->
                modNetwork.handleRegisterChannels(channels, handler.player)
        );
        ServerPlayNetworking.registerGlobalReceiver(PLASMO_VOICE, ((ServerNetworkHandlerFabric) network)::handle);

        ServerPlayConnectionEvents.JOIN.register(((handler, sender, server) ->
                modNetwork.handleJoin(handler.player))
        );

        ServerPlayConnectionEvents.DISCONNECT.register(((handler, server) ->
                modNetwork.handleQuit(handler.player))
        );

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) ->
                CommandManager.register(dispatcher)
        );
    }

    @Override
    protected void start() {
        super.start();
    }

    @Override
    protected void close() {
        network.close();
        super.close();
    }

    @Override
    public void updateConfig() {
        super.updateConfig();

        int clientModCheckTimeout = config.getInt("client_mod_check_timeout");
        if (clientModCheckTimeout < 20) {
            LOGGER.warn("Client mod check timeout cannot be < 20 ticks");
            return;
        }

        serverConfig = new ServerConfigFabric(getServerConfig(), config.getBoolean("client_mod_required"), clientModCheckTimeout);
    }

    @Override
    public int[] getVersion() {
        return version;
    }

    private void loadVersion() {
        ModContainer modContainer = FabricLoader.getInstance()
                .getModContainer("plasmo_voice")
                .orElse(null);
        if (modContainer == null) {
            return;
        }

        version = calculateVersion(modContainer.getMetadata().getVersion().getFriendlyString());
    }
}
