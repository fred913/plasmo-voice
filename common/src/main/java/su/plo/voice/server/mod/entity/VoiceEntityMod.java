package su.plo.voice.server.mod.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.ServerLevelData;
import su.plo.voice.api.Pos3d;
import su.plo.voice.api.entity.VoiceEntity;

import java.util.UUID;

public class VoiceEntityMod implements VoiceEntity {
    private final Entity entity;

    public VoiceEntityMod(Entity entity) {
        this.entity = entity;
    }

    @Override
    public int getEntityId() {
        return entity.getId();
    }

    @Override
    public UUID getUniqueId() {
        return entity.getUUID();
    }

    @Override
    public boolean isAlive() {
        return entity.isAlive();
    }

    @Override
    public String getWorld() {
        return ((ServerLevelData) entity.level.getLevelData()).getLevelName();
    }

    @Override
    public Pos3d getPosition() {
        return new Pos3d(entity.getX(), entity.getY(), entity.getZ());
    }
}
