package su.plo.voice.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.Entity;
import su.plo.voice.client.VoiceClient;

public class VoiceEntityRenderer {
    @Getter
    private static final VoiceEntityRenderer instance = new VoiceEntityRenderer();

    private final Minecraft client = Minecraft.getInstance();

    public void render(Entity entity, double distance, PoseStack matrices, boolean hasLabel, MultiBufferSource vertexConsumers, int light) {
        if (VoiceClient.getClientConfig().showIcons.get() == 2) {
            return;
        }

        if (entity.getUUID().equals(client.player.getUUID())) {
            return;
        }

        if (client.options.hideGui && VoiceClient.getClientConfig().showIcons.get() == 0) {
            return;
        }

        Boolean isTalking = VoiceClient.getNetwork().getTalkingEntity(entity.getId());
        if (isTalking != null) {
            if (isTalking) {
                renderIcon(96, entity, distance, matrices, hasLabel, vertexConsumers, light);
            } else {
                renderIcon(64, entity, distance, matrices, hasLabel, vertexConsumers, light);
            }
        }
    }

    private void renderIcon(float u, Entity entity, double distance, PoseStack matrices,
                                   boolean hasLabel, MultiBufferSource vertexConsumers, int light) {
        double yOffset = 0.5D;
        if (hasLabel) {
            yOffset += 0.3D;

//            Scoreboard scoreboard = entity.getScoreboard();
//            Objective scoreboardObjective = scoreboard.getDisplayObjective(2);
//            if (scoreboardObjective != null && distance < 100.0D) {
//                yOffset += 0.3D;
//            }
        }

        matrices.pushPose();
        matrices.translate(0D, entity.getBbHeight() + yOffset, 0D);
        matrices.mulPose(client.getEntityRenderDispatcher().cameraOrientation());
        matrices.scale(-0.025F, -0.025F, 0.025F);
        matrices.translate(0D, -1D, 0D);

        float offset = -5F;

        VertexConsumer builder = vertexConsumers.getBuffer(RenderType.text(VoiceClient.ICONS));

        float u0 = u / (float) 256;
        float u1 = (u + (float) 16) / (float) 256;
        float v0 = 0;
        float v1 = 0.0625F;

        vertex(builder, matrices, offset, 10F, 0F, u0, v1, 255, light);
        vertex(builder, matrices, offset + 10F, 10F, 0F, u1, v1, 255, light);
        vertex(builder, matrices, offset + 10F, 0F, 0F, u1, v0, 255, light);
        vertex(builder, matrices, offset, 0F, 0F, u0, v0, 255, light);

        VertexConsumer builderSeeThrough = vertexConsumers.getBuffer(RenderType.textSeeThrough(VoiceClient.ICONS));
        vertex(builderSeeThrough, matrices, offset, 10F, 0F, u0, v1, 40, light);
        vertex(builderSeeThrough, matrices, offset + 10F, 10F, 0F, u1, v1, 40, light);
        vertex(builderSeeThrough, matrices, offset + 10F, 0F, 0F, u1, v0, 40, light);
        vertex(builderSeeThrough, matrices, offset, 0F, 0F, u0, v0, 40, light);

        matrices.popPose();
    }

    private static void vertex(VertexConsumer builder, PoseStack matrices, float x, float y, float z, float u, float v, int alpha, int light) {
        PoseStack.Pose entry = matrices.last();
        Matrix4f modelViewMatrix = entry.pose();

        builder.vertex(modelViewMatrix, x, y, z);
        builder.color(255, 255, 255, alpha);
        builder.uv(u, v);
        builder.overlayCoords(OverlayTexture.NO_OVERLAY);
        builder.uv2(light);
        builder.normal(0F, 0F, -1F);
        builder.endVertex();
    }
}
