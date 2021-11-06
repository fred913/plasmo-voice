package su.plo.voice.client.config;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import su.plo.voice.client.VoiceClient;
import su.plo.voice.client.render.SphereRenderer;
import su.plo.voice.client.socket.SocketClientUDPListener;
import su.plo.voice.protocol.packets.tcp.ConfigS2CPacket;
import su.plo.voice.protocol.packets.tcp.PermissionsS2CPacket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
public class ServerSettings {
    private String secret;
    private String ip;
    private int port;
    @Setter(AccessLevel.PRIVATE)
    private List<Integer> distances = new ArrayList<>();
    private short distance;
    private short defaultDistance;
    private short minDistance;
    private short maxDistance;
    private short maxPriorityDistance;
    private short priorityDistance;
    private int fadeDivisor;
    private int priorityFadeDivisor;

    private boolean canSpeak;
    private boolean priority;
    private boolean activation;
    // todo impl
    private boolean forceSoundOcclusion;
    private int forceDistance;
    private int forcePriorityDistance;

    public ServerSettings(String secret, String ip, int port) {
        this.secret = secret;
        this.ip = ip;
        this.port = port;
    }

    public void update(PermissionsS2CPacket packet) {
        if (packet.getSpeak() != null) {
            this.canSpeak = packet.getSpeak();
        }
        if (packet.getPriority() != null) {
            this.priority = packet.getPriority();
        }
        if (packet.getActivation() != null) {
            this.activation = packet.getActivation();
        }
        if (packet.getForceSoundOcclusion() != null) {
            this.forceSoundOcclusion = packet.getForceSoundOcclusion();
        }
    }

    public void update(ConfigS2CPacket config) {
        this.update((PermissionsS2CPacket) config);
        this.distances = config.getDistances();
        Collections.sort(this.distances);
        this.minDistance = this.distances.get(0).shortValue();
        this.maxDistance = this.distances.get(this.distances.size() - 1).shortValue();
        this.maxPriorityDistance = (short) config.getMaxPriorityDistance();
        this.maxPriorityDistance = this.maxPriorityDistance == 0 ? Short.MAX_VALUE : this.maxPriorityDistance;
        this.fadeDivisor = config.getFadeDivisor();
        this.priorityFadeDivisor = config.getPriorityFadeDivisor();
        this.defaultDistance = (short) config.getDefaultDistance();

        if(VoiceClient.getClientConfig().getServers().containsKey(ip)) {
            ClientConfig.ServerConfig serverConfig = VoiceClient.getClientConfig().getServers().get(ip);
            serverConfig.distance.setDefault((int) this.defaultDistance);
            serverConfig.priorityDistance.setDefault(Math.min(this.maxPriorityDistance, this.maxDistance * 2));

            if(this.distances.contains(serverConfig.distance.get())) {
                this.distance = serverConfig.distance.get().shortValue();
            } else {
                this.distance = (short) config.getDefaultDistance();
            }

            if(serverConfig.priorityDistance.get() > this.maxDistance &&
                    serverConfig.priorityDistance.get() < this.maxPriorityDistance) {
                this.priorityDistance = serverConfig.priorityDistance.get().shortValue();
            } else {
                this.priorityDistance = (short) Math.min(this.maxPriorityDistance, this.maxDistance * 2);
            }
        } else {
            this.distance = (short) config.getDefaultDistance();
            this.priorityDistance = (short) Math.min(this.maxPriorityDistance, this.maxDistance * 2);

            ClientConfig.ServerConfig serverConfig = new ClientConfig.ServerConfig();
            serverConfig.distance.setDefault(config.getDefaultDistance());
            serverConfig.priorityDistance.setDefault(Math.min(this.maxPriorityDistance, this.maxDistance * 2));

            VoiceClient.getClientConfig().getServers().put(ip, serverConfig);
        }

        VoiceClient.setSpeaking(false);
        VoiceClient.setSpeakingPriority(false);

        VoiceClient.recorder.updateSampleRate(config.getSampleRate());

        SocketClientUDPListener.closeAll();

        SphereRenderer.getInstance().setRadius(this.distance + 0.5F, false, false);
    }

    public void setDistance(short distance) {
        this.distance = distance;
        SphereRenderer.getInstance().setRadius(this.distance + 0.5F, false);
    }

    public void setPriorityDistance(short distance) {
        this.priorityDistance = distance;
        SphereRenderer.getInstance().setRadius(this.priorityDistance + 0.5F, true);
    }
}
