package su.plo.voice.server;

import org.jetbrains.annotations.Nullable;
import su.plo.voice.api.DurationUnit;
import su.plo.voice.api.PlasmoVoiceAPI;
import su.plo.voice.api.ServerMuteEntry;
import su.plo.voice.api.entity.EntityManager;
import su.plo.voice.api.event.EventBus;
import su.plo.voice.api.event.player.VoiceMuteEvent;
import su.plo.voice.api.event.player.VoiceUnmuteEvent;
import su.plo.voice.api.player.PlayerManager;
import su.plo.voice.api.player.VoicePlayer;
import su.plo.voice.api.sources.SourceManager;
import su.plo.voice.protocol.packets.tcp.ClientMutedS2CPacket;
import su.plo.voice.protocol.packets.tcp.ClientUnmutedS2CPacket;
import su.plo.voice.server.socket.SocketServerUDP;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class VoiceServerAPIImpl implements PlasmoVoiceAPI {
    private final SourceManagerImpl sources = new SourceManagerImpl();
    private final EventBus eventBus = new EventBusImpl();
    private final ConcurrentHashMap<UUID, ServerMuteEntry> muted = new ConcurrentHashMap<>();
    private final PlayerManager playerManager;
    private final EntityManager entityManager;

    public VoiceServerAPIImpl(PlayerManager playerManager, EntityManager entityManager) {
        this.playerManager = playerManager;
        this.entityManager = entityManager;
    }

    @Override
    public void mute(UUID playerId, long duration, @Nullable DurationUnit durationUnit, @Nullable String reason, boolean silent) {
        VoicePlayer player = playerManager.getByUniqueId(playerId);
        if (player == null) {
            throw new NullPointerException("Player not found");
        }

        if (duration > 0 && durationUnit == null) {
            throw new NullPointerException("durationUnit cannot be null if duration > 0");
        }

        String durationMessage = durationUnit == null ? "" : durationUnit.format(VoiceServer.getInstance(), duration);
        if (duration > 0) {
            duration = durationUnit.multiply(duration) * 1000;
            duration += System.currentTimeMillis();
        }

        ServerMuteEntry mute = new ServerMuteEntry(player.getUniqueId(), duration, reason);
        VoiceMuteEvent event = new VoiceMuteEvent(player, mute);
        ((EventBusImpl) VoiceServer.getAPI().getEventBus()).post(event);
        if (event.isCancelled()) {
            return;
        }

        muted.put(player.getUniqueId(), mute);
        VoiceServer.getInstance().saveData(true);

        if (player.isOnline()) {
            VoiceServer.getNetwork().sendToClients(new ClientMutedS2CPacket(mute.getUuid()), player);

            if (!silent) {
                player.sendMessage((duration > 0
                        ? VoiceServer.getInstance().getMessagePrefix("player_muted")
                        : VoiceServer.getInstance().getMessagePrefix("player_muted_perm"))
                        .replace("{duration}", durationMessage)
                        .replace("{reason}", reason != null
                                ? reason
                                : VoiceServer.getInstance().getMessage("mute_no_reason")
                        )
                );
            }
        }
    }

    @Override
    public boolean unmute(UUID playerId, boolean silent) {
        VoicePlayer player = playerManager.getByUniqueId(playerId);
        if (player == null) {
            return false;
        }

        ServerMuteEntry mute = this.muted.get(player.getUniqueId());
        if (mute == null) {
            return false;
        }

        VoiceUnmuteEvent event = new VoiceUnmuteEvent(player, mute);
        ((EventBusImpl) VoiceServer.getAPI().getEventBus()).post(event);
        if (event.isCancelled()) {
            return false;
        }

        this.muted.remove(mute.getUuid());
        VoiceServer.getInstance().saveData(true);

        if (player.isOnline()) {
            VoiceServer.getNetwork().sendToClients(new ClientUnmutedS2CPacket(player.getUniqueId()), player);

            if (!silent) {
                player.sendMessage(VoiceServer.getInstance().getMessagePrefix("player_unmuted"));
            }
        }

        return mute.getTo() <= 0 || mute.getTo() >= System.currentTimeMillis();
    }

    @Override
    public boolean isMuted(UUID playerId) {
        ServerMuteEntry e = muted.get(playerId);
        if (e == null) {
            return false;
        }

        if (e.getTo() == 0 || e.getTo() > System.currentTimeMillis()) {
            return true;
        } else {
            muted.remove(e.getUuid());

            VoicePlayer player = playerManager.getByUniqueId(playerId);
            if (player != null && player.isOnline()) {
                VoiceServer.getNetwork().sendToClients(new ClientUnmutedS2CPacket(e.getUuid()), player);
            }

            return false;
        }
    }

    @Override
    public Map<UUID, ServerMuteEntry> getMutedMap() {
        return muted;
    }

    @Override
    public boolean hasVoiceChat(UUID player) {
        return SocketServerUDP.clients.entrySet()
                .stream()
                .anyMatch(entry -> entry.getKey().equals(player));
    }

    @Nullable
    @Override
    public String getPlayerModLoader(UUID player) {
        return SocketServerUDP.clients
                .values()
                .stream()
                .filter(c -> c.getPlayer().getUniqueId().equals(player))
                .findFirst()
                .map(client -> client.getPlayer().getModLoader())
                .orElse(null);
    }

    @Override
    public List<UUID> getPlayers() {
        return SocketServerUDP.clients
                .values()
                .stream()
                .map(client -> client.getPlayer().getUniqueId())
                .collect(Collectors.toList());
    }

    @Override
    public int getPlayerCount() {
        return SocketServerUDP.clients.size();
    }

    @Override
    public EventBus getEventBus() {
        return eventBus;
    }

    @Override
    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    @Override
    public EntityManager getEntityManager() {
        return entityManager;
    }

    @Override
    public SourceManager getSourceManager() {
        return sources;
    }
}
