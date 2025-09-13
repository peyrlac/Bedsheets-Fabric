package be.pierrelac.bedsheets.client.mixin;

import be.pierrelac.bedsheets.client.render.BedSheetRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BedBlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BedBlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Client-side mixin for BedBlockEntityRenderer to add custom sheet rendering.
 * Renders bed sheets after the normal bed model.
 */
@Environment(EnvType.CLIENT)
@Mixin(BedBlockEntityRenderer.class)
public class BedBlockEntityRendererMixin {
    
    /**
     * Inject after normal bed rendering to add our custom sheet.
     */
    @Inject(method = "render(Lnet/minecraft/block/entity/BedBlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V", 
            at = @At("TAIL"))
    private void bedsheets$render(BedBlockEntity bedBlockEntity, float tickDelta, MatrixStack matrices,
                                VertexConsumerProvider vertexConsumers, int light, int overlay, CallbackInfo ci) {
        
        // Render our custom bed sheet
        BedSheetRenderer.renderSheet(bedBlockEntity, matrices, vertexConsumers, light, overlay);
    }
}