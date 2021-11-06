package su.plo.voice.server.mod.config;

import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import lombok.Data;
import su.plo.voice.api.ServerMuteEntry;
import su.plo.voice.server.config.ServerData;

import java.io.*;
import java.util.*;

@Data
public class ServerDataMod extends ServerData {
    private final Map<UUID, Map<String, Boolean>> permissions;

    public ServerDataMod() {
        super();
        this.permissions = new HashMap<>();
    }

    public ServerDataMod(List<ServerMuteEntry> mutes, Map<UUID, Map<String, Boolean>> permissions) {
        super(mutes);
        this.permissions = permissions;
    }

    public static ServerDataMod read(File dataFolder) {
        File dataFile = new File(dataFolder, "server_data.json");
        if(dataFile.exists()) {
            try {
                JsonReader reader = new JsonReader(new FileReader(dataFile));
                try {
                    return gson.fromJson(reader, ServerDataMod.class);
                } catch (JsonSyntaxException j) {
                    dataFile.delete();
                }
            } catch (FileNotFoundException ignored) {}
        }

        return new ServerDataMod(new ArrayList<>(), new HashMap<>());
    }

    public static void saveAsync(File dataFolder, ServerDataMod data) {
        new Thread(() -> {
            dataFolder.mkdirs();

            try {
                try (Writer w = new FileWriter(new File(dataFolder, "server_data.json"))) {
                    w.write(gson.toJson(data));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void save(File dataFolder, ServerDataMod data) {
        dataFolder.mkdirs();

        try {
            try (Writer w = new FileWriter(new File(dataFolder, "server_data.json"))) {
                w.write(gson.toJson(data));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
