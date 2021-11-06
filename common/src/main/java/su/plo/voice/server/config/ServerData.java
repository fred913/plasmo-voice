package su.plo.voice.server.config;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import lombok.AllArgsConstructor;
import lombok.Data;
import su.plo.voice.api.ServerMuteEntry;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Data
public class ServerData {
    protected final static Gson gson = new Gson();

    private final List<ServerMuteEntry> muted;

    public ServerData() {
        this.muted = new ArrayList<>();
    }

    public static ServerData read(File dataFolder) {
        File dataFile = new File(dataFolder, "server_data.json");
        if(dataFile.exists()) {
            try {
                JsonReader reader = new JsonReader(new FileReader(dataFile));
                try {
                    return gson.fromJson(reader, ServerData.class);
                } catch (JsonSyntaxException j) {
                    dataFile.delete();
                }
            } catch (FileNotFoundException ignored) {}
        }

        return new ServerData();
    }

    public static void saveAsync(File dataFolder, ServerData data) {
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

    public static void save(File dataFolder, ServerData data) {
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
