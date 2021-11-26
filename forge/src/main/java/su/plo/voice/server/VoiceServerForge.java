package su.plo.voice.server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fmlserverevents.FMLServerStartedEvent;
import net.minecraftforge.fmlserverevents.FMLServerStoppingEvent;
import su.plo.voice.server.mod.VoiceServerMod;
import su.plo.voice.server.mod.commands.CommandManager;
import su.plo.voice.server.network.ServerNetworkHandlerForge;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class VoiceServerForge extends VoiceServerMod {
    private final ServerNetworkHandlerForge modNetwork;
    private int[] version = new int[3];

    public VoiceServerForge() {
        super(new ServerNetworkHandlerForge());
        this.modNetwork = (ServerNetworkHandlerForge) network;
        loadVersion();
    }

    @Override
    protected void start() {
        network.start();
        super.start();
    }

    @Override
    protected void close() {
        network.close();
        super.close();
    }

    @Override
    public int[] getVersion() {
        return version;
    }

    private void loadVersion() {
        version = calculateVersion(ModList.get().getModFileById("plasmo_voice").versionString());
    }

    public void onChannelRegister(ServerboundCustomPayloadPacket packet, ServerPlayer player) {
        FriendlyByteBuf buffer = packet.getData();
        byte[] data = new byte[Math.max(buffer.readableBytes(), 0)];
        buffer.readBytes(data);

        modNetwork.handleRegisterChannels(bytesToResLocation(data), player);
    }

    private static List<ResourceLocation> bytesToResLocation(byte[] all) {
        List<ResourceLocation> rl = new ArrayList<>();
        int last = 0;
        for (int cur = 0; cur < all.length; cur++) {
            if (all[cur] == '\0') {
                String s = new String(all, last, cur - last, StandardCharsets.UTF_8);
                rl.add(new ResourceLocation(s));
                last = cur + 1;
            }
        }
        return rl;
    }

    @SubscribeEvent
    public void onCommandRegister(RegisterCommandsEvent event) {
        CommandManager.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            modNetwork.handleJoin(player);
        }
    }

    @SubscribeEvent
    public void onPlayerQuit(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            modNetwork.handleQuit(player);
        }
    }

    @SubscribeEvent
    public void onServerStart(FMLServerStartedEvent event) {
        setServer(event.getServer());
        this.start();

        if (event.getServer().isDedicatedServer()) {
            this.setupMetrics("Forge");
        }
    }

    @SubscribeEvent
    public void onServerStop(FMLServerStoppingEvent event) {
        this.close();
    }
}