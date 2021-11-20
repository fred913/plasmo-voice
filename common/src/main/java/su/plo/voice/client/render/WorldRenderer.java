package su.plo.voice.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import lombok.Getter;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.Vec3;
import su.plo.voice.client.VoiceClient;
import su.plo.voice.protocol.sources.SourceInfo;
import su.plo.voice.protocol.sources.StaticSourceInfo;

import java.util.Map;

public class WorldRenderer {
    @Getter
    private static final WorldRenderer instance = new WorldRenderer();

    private final BufferBuilder bufferBuilder = new BufferBuilder(2097152);
    private final VertexBuffer vertexBuffer = new VertexBuffer();

    public void render(PoseStack matrices, Camera camera, Matrix4f matrix4f, Minecraft client) {
        if (!VoiceClient.isConnected()) {
            return;
        }

        for (Map.Entry<SourceInfo, Boolean> entry : VoiceClient.getNetwork().getTalkingSources().entrySet()) {
            SourceInfo info = entry.getKey();
            Boolean talking = entry.getValue();

            if (info instanceof StaticSourceInfo && info.isVisible()) {
                StaticSourceInfo staticInfo = (StaticSourceInfo) info;
                Vec3 position = new Vec3(staticInfo.getPosition().getX(), staticInfo.getPosition().getY(), staticInfo.getPosition().getZ());
                Vec3 cameraPos = camera.getPosition();

                if (cameraPos.distanceToSqr(position) > 4096.0D) {
                    continue;
                }

                float u0, u1;
                float v0 = 0;
                float v1 = 0.0625F;
                if (talking) { // priority
                    u0 = 96 / (float) 256;
                    u1 = (96 + (float) 16) / (float) 256;
                } else {
                    u0 = 64 / (float) 256;
                    u1 = (64 + (float) 16) / (float) 256;
                }

                Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();
                RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
                RenderSystem.setShaderTexture(0, VoiceClient.ICONS);

                matrices.pushPose();

                // scale & translate matrices
                matrices.translate(position.x() - cameraPos.x(), position.y() - cameraPos.y(), position.z() - cameraPos.z());
                matrices.mulPose(camera.rotation());
                matrices.scale(-0.025F, -0.025F, 0.025F);

                // default icons
                RenderSystem.setShader(GameRenderer::getPositionColorTexLightmapShader);
                bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP);

                vertex(bufferBuilder, 0F, 10F, 0F, u0, v1, 255, 15);
                vertex(bufferBuilder, 10F, 10F, 0F, u1, v1, 255, 15);
                vertex(bufferBuilder, 10F, 0F, 0F, u1, v0, 255, 15);
                vertex(bufferBuilder, 0F, 0F, 0F, u0, v0, 255, 15);

                bufferBuilder.end();
                vertexBuffer.uploadLater(bufferBuilder);

                vertexBuffer.drawWithShader(matrices.last().pose(), matrix4f, GameRenderer.getPositionColorTexLightmapShader());

                // see through icon
                RenderSystem.setShader(GameRenderer::getPositionColorTexLightmapShader);
                RenderSystem.disableDepthTest();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.depthMask(true);
                bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);

                vertex(bufferBuilder, 0F, 10F, 0F, u0, v1, 40, 15);
                vertex(bufferBuilder, 10F, 10F, 0F, u1, v1, 40, 15);
                vertex(bufferBuilder, 10F, 0F, 0F, u1, v0, 40, 15);
                vertex(bufferBuilder, 0F, 0F, 0F, u0, v0, 40, 15);

                bufferBuilder.end();
                vertexBuffer.uploadLater(bufferBuilder);

                vertexBuffer.drawWithShader(matrices.last().pose(), matrix4f, GameRenderer.getPositionColorTexLightmapShader());


                matrices.popPose();

                RenderSystem.enableDepthTest();
                RenderSystem.disableBlend();
                RenderSystem.depthFunc(519);
                RenderSystem.depthMask(true);
                Minecraft.getInstance().gameRenderer.lightTexture().turnOffLightLayer();
            }
        }
    }

    private void vertex(VertexConsumer builder, double x, double y, double z, float u, float v, int alpha, int light) {
        builder.vertex((float) x, (float) y, (float) z)
                .color(255, 255, 255, alpha)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(0F, 0F, -1F)
                .endVertex();
    }
}
