package su.plo.voice.server.mod.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import su.plo.voice.api.entity.EntityManager;
import su.plo.voice.api.entity.VoiceEntity;
import su.plo.voice.server.mod.VoiceServerMod;

import java.util.UUID;

public class EntityManagerMod implements EntityManager {
    @Override
    public VoiceEntity getByUniqueId(UUID entityId) {
        for (ServerLevel level : VoiceServerMod.getServer().getAllLevels()) {
            Entity entity = level.getEntity(entityId);
            if (entity != null) {
                return new VoiceEntityMod(entity);
            }
        }

        return null;
    }
}
