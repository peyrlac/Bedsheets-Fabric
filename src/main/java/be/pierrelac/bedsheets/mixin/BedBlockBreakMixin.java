package be.pierrelac.bedsheets.mixin;

import be.pierrelac.bedsheets.BedSheetsMod;
import be.pierrelac.bedsheets.data.SheetPattern;
import be.pierrelac.bedsheets.util.BannerUtils;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BedBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.BedPart;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin for BedBlock to handle banner drops when breaking beds with patterns.
 */
@Mixin(BedBlock.class)
public class BedBlockBreakMixin {
    
    /**
     * Inject when a bed is broken to drop the banner if configured.
     */
    @Inject(method = "onBreak", at = @At("HEAD"))
    private void bedsheets$onBreak(World world, BlockPos pos, BlockState state, 
                                 net.minecraft.entity.player.PlayerEntity player, CallbackInfo ci) {
        
        if (world.isClient() || !BedSheetsMod.getConfig().dropBannerOnBreak) {
            return;
        }
        
        // Only handle head blocks to avoid duplicate drops
        if (state.get(BedBlock.PART) != BedPart.HEAD) {
            return;
        }
        
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof BedBlockEntity bedBlockEntity)) {
            return;
        }
        
        // Check if bed has a pattern to drop
        if (blockEntity instanceof BedBlockEntityAccessor accessor) {
            SheetPattern pattern = accessor.bedsheets$getSheetPattern();
            if (pattern != null && pattern.hasPattern()) {
                
                // Create banner item from pattern
                ItemStack bannerDrop = BannerUtils.toBanner(pattern);
                if (!bannerDrop.isEmpty()) {
                    
                    // Drop the banner at the bed position
                    if (world instanceof ServerWorld serverWorld) {
                        ItemEntity itemEntity = new ItemEntity(serverWorld, 
                                                               pos.getX() + 0.5, 
                                                               pos.getY() + 0.5, 
                                                               pos.getZ() + 0.5, 
                                                               bannerDrop);
                        itemEntity.setVelocity(0, 0.1, 0);
                        serverWorld.spawnEntity(itemEntity);
                        
                        BedSheetsMod.LOGGER.debug("Dropped banner from broken bed at {}", pos);
                    }
                }
            }
        }
    }
}