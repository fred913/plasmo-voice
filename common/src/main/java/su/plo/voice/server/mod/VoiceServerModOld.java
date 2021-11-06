//package su.plo.voice.server.mod;
//
//import lombok.Getter;
//import lombok.Setter;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.server.MinecraftServer;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//import su.plo.voice.server.api.PlasmoVoiceAPI;
//import su.plo.voice.server.config.*;
//import su.plo.voice.server.mod.config.ServerDataMod;
//import su.plo.voice.server.mod.metrics.Metrics;
//import su.plo.voice.server.mod.player.PlayerManagerMod;
//import su.plo.voice.server.socket.SocketServerUDP;
//
//import java.io.File;
//import java.io.IOException;
//import java.io.InputStream;
//import java.nio.file.Files;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.UUID;
//
//public abstract class VoiceServerModOld {
//    @Getter
//    private static VoiceServerModOld instance;
//
//    public static final Logger LOGGER = LogManager.getLogger("Plasmo Voice");
//
//    @Getter
//    protected static ServerNetworkHandler network;
//
//    @Getter
//    @Setter
//    private static MinecraftServer server;
//
//    public static final ResourceLocation PLASMO_VOICE = new ResourceLocation("plasmo:voice");
//    public static final UUID NIL_UUID = new UUID(0, 0);
//
//    @Getter
//    private static final PlayerManagerMod playerManager = new PlayerManagerMod();
//
//    // protocol version
//    public static int protocolVersion;
//
//    @Getter
//    @Setter
//    private static ServerConfig serverConfig;
//    private SocketServerUDP udpServer;
//
//    @Getter
//    protected Configuration config;
//
//    @Getter
//    private static PlasmoVoiceAPI API;
//
//    private Metrics metrics;
//
//    protected void start() {
//        instance = this;
//        loadConfig();
//        updateConfig();
//
//        API = new VoiceServerModAPIImpl();
//
//        udpServer = new SocketServerUDP(serverConfig.getIp(), serverConfig.getPort());
//        udpServer.start();
//    }
//
//    protected void close() {
//        if (udpServer != null) {
//            udpServer.close();
//        }
//
//        if (metrics != null) {
//            metrics.close();
//        }
//
//        ServerNetworkHandler.playerToken.clear();
//
//        saveData(false);
//    }
//
//
//    protected void setupMetrics(String software) {
//        metrics = new Metrics(10928, software.toLowerCase(), server);
//        metrics.addCustomChart(new Metrics.SingleLineChart("players_with_forge_mod", () ->
//                (int) SocketServerUDP.clients.values().stream().filter(s -> s.getType().equals("forge")).count()));
//        metrics.addCustomChart(new Metrics.SingleLineChart("players_with_fabric_mod", () ->
//                (int) SocketServerUDP.clients.values().stream().filter(s -> s.getType().equals("fabric")).count()));
//
//        metrics.addCustomChart(new Metrics.SimplePie("server_type", () -> software));
//    }
//
//    public File getDataFolder() {
//        return new File("config/PlasmoVoice");
//    }
//
//    protected void loadData() {
//        ServerDataMod serverData = ServerDataMod.read();
//        if (serverData != null) {
//            for (ServerMuted e : serverData.getMuted()) {
////                muted.put(e.getUuid(), e);
//            }
//
//            playerManager.getPermissions().putAll(serverData.getPermissions());
//        }
//    }
//
//    public void saveData(boolean async) {
//        if (async) {
//            ServerData.saveAsync(new ServerData(new ArrayList<>(muted.values()), playerManager.getPermissions()));
//        } else {
//            ServerData.save(new ServerData(new ArrayList<>(muted.values()), playerManager.getPermissions()));
//        }
//    }
//
//    public void loadConfig() {
//        try {
//            // Save default config
//            File configDir = getDataFolder();
//            configDir.mkdirs();
//
//            File file = new File(configDir, "server.yml");
//
//            if (!file.exists()) {
//                try (InputStream in = this.getClass().getClassLoader().getResourceAsStream("server.yml")) {
//                    Files.copy(in, file.toPath());
//                }
//            }
//
//            Configuration defaults;
//            try (InputStream in = this.getClass().getClassLoader().getResourceAsStream("server.yml")) {
//                defaults = ConfigurationProvider.getProvider(YamlConfiguration.class)
//                        .load(in);
//            }
//
//            // Load config
//            config = ConfigurationProvider.getProvider(YamlConfiguration.class)
//                    .load(file, defaults);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void updateConfig() {
//        if (!config.getDefault("config_version").equals(config.getString("config_version"))) {
//            LOGGER.warn("Config outdated. Check https://github.com/plasmoapp/plasmo-voice/wiki/How-to-install-Server for new config.");
//        }
//
//        int sampleRate = config.getInt("sample_rate");
//        if (sampleRate != 8000 && sampleRate != 12000 && sampleRate != 24000 && sampleRate != 48000) {
//            LOGGER.warn("Sample rate cannot be " + sampleRate);
//            return;
//        }
//
//        List<Integer> distances = config.getIntList("distances");
//        if (distances.size() == 0) {
//            LOGGER.warn("Distances cannot be empty");
//            return;
//        }
//        int defaultDistance = config.getInt("default_distance");
//        if (defaultDistance == 0) {
//            defaultDistance = distances.get(0);
//        } else if (!distances.contains(defaultDistance)) {
//            defaultDistance = distances.get(0);
//        }
//
//        int fadeDivisor = config.getInt("fade_divisor");
//        if (fadeDivisor <= 0) {
//            LOGGER.warn("Fade distance cannot be <= 0");
//            return;
//        }
//
//        int priorityFadeDivisor = config.getInt("priority_fade_divisor");
//        if (priorityFadeDivisor <= 0) {
//            LOGGER.warn("Priority fade distance cannot be <= 0");
//            return;
//        }
//
//        int udpPort = config.getInt("udp.port");
//        if (udpPort == 0) {
//            udpPort = server.getPort();
//
//            // integrated server, use 60606
//            if (udpPort == -1) {
//                udpPort = 60606;
//            }
//        }
//
//        serverConfig = new ServerConfig(
//                config.getString("udp.ip"),
//                udpPort,
//                config.getString("udp.proxy_ip"),
//                config.getInt("udp.proxy_port"),
//                sampleRate,
//                distances,
//                defaultDistance,
//                config.getInt("max_priority_distance"),
//                config.getBoolean("disable_voice_activation"),
//                fadeDivisor,
//                priorityFadeDivisor);
//    }
//
//    public static int getProtocolVersion(String s) {
//        int ver = 0;
//        String[] version = s.split("\\.");
//        try {
//            return Integer.parseInt(version[0]);
//        } catch (NumberFormatException ignored) {
//        }
//
//        return ver;
//    }
//
//    public static boolean isLogsEnabled() {
//        return !instance.getConfig().getBoolean("disable_logs");
//    }
//
//    // Get message with prefix from config
//    public String getMessagePrefix(String name) {
//        // Get message or use default value
//        String message = config.getString("messages." + name);
//        if (message == null) {
//            message = "";
//        } else {
//            message = message.replace('&', '\u00A7');
//        }
//
//        return this.getPrefix() + message;
//    }
//
//    // Get message from config
//    public String getMessage(String name) {
//        // Get message or use default value
//        String message = config.getString("messages." + name);
//        if (message == null) {
//            message = "";
//        } else {
//            message = message.replace('&', '\u00A7');
//        }
//
//        return message;
//    }
//
//    // Get plugin prefix from config
//    public String getPrefix() {
//        return config.getString("messages.prefix").replace('&', '\u00A7');
//    }
//}
