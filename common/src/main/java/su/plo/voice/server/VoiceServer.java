package su.plo.voice.server;

import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.api.ConfigMessages;
import su.plo.voice.api.PlasmoVoiceAPI;
import su.plo.voice.api.player.PlayerManager;
import su.plo.voice.server.config.Configuration;
import su.plo.voice.server.config.ConfigurationProvider;
import su.plo.voice.server.config.ServerConfig;
import su.plo.voice.server.config.YamlConfiguration;
import su.plo.voice.server.network.ServerNetworkHandler;
import su.plo.voice.server.socket.SocketServerUDP;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

public abstract class VoiceServer implements ConfigMessages {
    @Getter
    private static VoiceServer instance;

    public static final Logger LOGGER = LogManager.getLogger("Plasmo Voice");

    protected  final SourceManager sources = new SourceManager();
    protected final PlayerManager playerManager;
    protected final ServerNetworkHandler network;

    private SocketServerUDP udpServer;

    @Getter
    protected static ServerConfig serverConfig;

    @Getter
    protected Configuration config;

    @Getter
    protected static PlasmoVoiceAPI API;

    protected VoiceServer(PlayerManager playerManager, ServerNetworkHandler network) {
        this.playerManager = playerManager;
        this.network = network;
    }

    protected void start() {
        instance = this;
        loadConfig();
        updateConfig();
        loadData();

        udpServer = new SocketServerUDP(serverConfig.getIp(), serverConfig.getPort());
        udpServer.start();
    }

    protected void close() {
        if (udpServer != null) {
            udpServer.close();
        }

        saveData(true);
    }

    public void loadConfig() {
        try {
            // Save default config
            File configDir = getDataFolder();
            configDir.mkdirs();

            File file = new File(configDir, "config.yml");

            if (!file.exists()) {
                try (InputStream in = this.getClass().getClassLoader().getResourceAsStream("config.yml")) {
                    Files.copy(in, file.toPath());
                }
            }

            Configuration defaults;
            try (InputStream in = this.getClass().getClassLoader().getResourceAsStream("config.yml")) {
                defaults = ConfigurationProvider.getProvider(YamlConfiguration.class)
                        .load(in);
            }

            // Load config
            config = ConfigurationProvider.getProvider(YamlConfiguration.class)
                    .load(file, defaults);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateConfig() {
        if (!config.getDefault("config_version").equals(config.getString("config_version"))) {
            LOGGER.warn("Config outdated. Check https://github.com/plasmoapp/plasmo-voice/wiki/How-to-install-Server for new config.");
        }

        int sampleRate = config.getInt("sample_rate");
        if (sampleRate != 8000 && sampleRate != 12000 && sampleRate != 24000 && sampleRate != 48000) {
            LOGGER.warn("Sample rate cannot be " + sampleRate);
            return;
        }

        List<Integer> distances = config.getIntList("distances");
        if (distances.size() == 0) {
            LOGGER.warn("Distances cannot be empty");
            return;
        }
        int defaultDistance = config.getInt("default_distance");
        if (defaultDistance == 0) {
            defaultDistance = distances.get(0);
        } else if (!distances.contains(defaultDistance)) {
            defaultDistance = distances.get(0);
        }

        int fadeDivisor = config.getInt("fade_divisor");
        if (fadeDivisor <= 0) {
            LOGGER.warn("Fade distance cannot be <= 0");
            return;
        }

        int priorityFadeDivisor = config.getInt("priority_fade_divisor");
        if (priorityFadeDivisor <= 0) {
            LOGGER.warn("Priority fade distance cannot be <= 0");
            return;
        }

        int udpPort = config.getInt("udp.port");
        if (udpPort == 0) {
            udpPort = getPort();

            // integrated server, use 60606
            if (udpPort == -1) {
                udpPort = 60606;
            }
        }

        serverConfig = new ServerConfig(
                config.getString("udp.ip"),
                udpPort,
                config.getString("udp.proxy_ip"),
                config.getInt("udp.proxy_port"),
                sampleRate,
                distances,
                defaultDistance,
                config.getInt("max_priority_distance"),
                fadeDivisor,
                priorityFadeDivisor);
    }

    @Nullable
    @Override
    public String getMessagePrefix(String name) {
        // Get message or use default value
        String message = config.getString("messages." + name);
        if (message == null) {
            message = "";
        } else {
            message = message.replace('&', '\u00A7');
        }

        return this.getPrefix() + message;
    }

    @Nullable
    @Override
    public String getMessage(String name) {
        // Get message or use default value
        String message = config.getString("messages." + name);
        if (message == null) {
            message = "";
        } else {
            message = message.replace('&', '\u00A7');
        }

        return message;
    }

    @Nullable
    @Override
    public String getPrefix() {
        return config.getString("messages.prefix").replace('&', '\u00A7');
    }

    public static ServerNetworkHandler getNetwork() {
        return instance.network;
    }

    public static SourceManager getSources() {
        return instance.sources;
    }

    public static int[] calculateVersion(String s) {
        int[] version = new int[3];
        String[] split = s.split("\\.");
        try {
            version[0] = Integer.parseInt(split[0]);
            version[1] = Integer.parseInt(split[1]);
            version[2] = Integer.parseInt(split[2]);
        } catch (NumberFormatException ignored) {
        }

        return version;
    }

    public static boolean isLogsEnabled() {
        return !instance.getConfig().getBoolean("disable_logs");
    }

    public abstract void runAsync(Runnable runnable);

    public abstract int getPort();

    public abstract File getDataFolder();

    public abstract void loadData();

    public abstract void saveData(boolean async);

    public abstract int[] getVersion();
}
