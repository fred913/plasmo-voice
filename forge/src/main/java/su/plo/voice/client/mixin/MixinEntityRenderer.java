package su.plo.voice.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import su.plo.voice.client.VoiceClient;
import su.plo.voice.client.render.VoiceEntityRenderer;
import su.plo.voice.client.render.VoicePlayerRenderer;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer<T extends Entity> {
    @Shadow @Final protected EntityRenderDispatcher entityRenderDispatcher;

    @Shadow protected abstract boolean shouldShowName(T entity);

    @Inject(method = "render", at = @At(value = "HEAD"))
    public void render(T entity, float f, float g, PoseStack matrices, MultiBufferSource vertexConsumerProvider, int i, CallbackInfo ci) {
        if(!VoiceClient.isConnected()) {
            return;
        }

        double d = this.entityRenderDispatcher.distanceToSqr(entity);
        if (d > 4096.0D) {
            return;
        }

        if (entity instanceof AbstractClientPlayer) {
            VoicePlayerRenderer.getInstance().render((AbstractClientPlayer) entity, d, matrices, this.shouldShowName(entity), vertexConsumerProvider, i);
        } else {
            VoiceEntityRenderer.getInstance().render(entity, d, matrices, this.shouldShowName(entity), vertexConsumerProvider, i);
        }
    }
}
