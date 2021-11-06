package su.plo.voice.protocol.packets;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import java.io.IOException;

public interface Packet<T> {
    void read(ByteArrayDataInput buf) throws IOException;

    void write(ByteArrayDataOutput buf) throws IOException;

    void handle(T listener);
}