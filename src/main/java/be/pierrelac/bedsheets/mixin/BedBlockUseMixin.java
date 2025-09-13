package be.pierrelac.bedsheets.mixin;

import be.pierrelac.bedsheets.BedSheetsMod;
import be.pierrelac.bedsheets.data.SheetPattern;
import be.pierrelac.bedsheets.net.BedSheetsPackets;
import be.pierrelac.bedsheets.util.BannerUtils;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BedBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.BedPart;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BannerItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin for BedBlock to handle custom interactions.
 * Handles banner application, water bucket clearing, and bed making.
 */
@Mixin(BedBlock.class)
public class BedBlockUseMixin {
    
    /**
     * Inject into onUse to handle our custom interactions.
     */
    @Inject(method = "onUse", at = @At("HEAD"), cancellable = true)
    private void bedsheets$onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, 
                                Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        
        if (world.isClient()) {
            return; // Only handle on server side
        }
        
        ItemStack heldItem = player.getStackInHand(hand);
        boolean isHead = state.get(BedBlock.PART) == BedPart.HEAD;
        BlockPos headPos = isHead ? pos : pos.offset(state.get(BedBlock.FACING));
        
        // Get bed block entity (always use head position)
        BlockEntity blockEntity = world.getBlockEntity(headPos);
        if (!(blockEntity instanceof BedBlockEntity bedBlockEntity)) {
            return;
        }
        
        BedBlockEntityAccessor accessor = (BedBlockEntityAccessor) blockEntity;
        
        // Handle banner application
        if (heldItem.getItem() instanceof BannerItem) {
            if (handleBannerApplication(world, headPos, heldItem, player, accessor, bedBlockEntity)) {
                cir.setReturnValue(ActionResult.SUCCESS);
                return;
            }
        }
        
        // Handle water bucket clearing (shift + right-click)
        if (player.isSneaking() && heldItem.isOf(Items.WATER_BUCKET)) {
            if (handleWaterBucketClear(world, headPos, player, accessor, bedBlockEntity)) {
                cir.setReturnValue(ActionResult.SUCCESS);
                return;
            }
        }
        
        // Handle bed making (shift + empty hand on head block)
        if (player.isSneaking() && heldItem.isEmpty() && isHead) {
            if (handleBedMaking(world, headPos, player, accessor, bedBlockEntity)) {
                cir.setReturnValue(ActionResult.SUCCESS);
                return;
            }
        }
    }
    
    /**
     * Handles applying a banner pattern to the bed.
     */
    private boolean handleBannerApplication(World world, BlockPos headPos, ItemStack banner, 
                                          PlayerEntity player, BedBlockEntityAccessor accessor,
                                          BedBlockEntity bedBlockEntity) {
        
        // Extract pattern from banner
        SheetPattern pattern = BannerUtils.fromBanner(banner);
        if (pattern == null) {
            return false;
        }
        
        // Check if bed is occupied
        if (accessor.bedsheets$isOccupied()) {
            // TODO: Send message to player that bed is occupied
            return false;
        }
        
        // Apply pattern
        accessor.bedsheets$setSheetPattern(pattern);
        accessor.bedsheets$setSheetOwner(player.getUuid());
        bedBlockEntity.markDirty();
        
        // Consume banner (unless creative mode)
        if (!player.getAbilities().creativeMode) {
            banner.decrement(1);
        }
        
        // Play sound
        world.playSound(null, headPos, SoundEvents.BLOCK_WOOL_PLACE, SoundCategory.BLOCKS, 
                       0.8f, 0.8f + world.random.nextFloat() * 0.4f);
        
        // Sync to clients
        if (player instanceof ServerPlayerEntity serverPlayer) {
            BedSheetsPackets.sendPatternUpdate(serverPlayer, headPos, pattern);
        }
        
        BedSheetsMod.LOGGER.debug("Applied banner pattern to bed at {}", headPos);
        return true;
    }
    
    /**
     * Handles clearing the bed pattern with water bucket.
     */
    private boolean handleWaterBucketClear(World world, BlockPos headPos, PlayerEntity player,
                                         BedBlockEntityAccessor accessor, BedBlockEntity bedBlockEntity) {
        
        if (!BedSheetsMod.getConfig().allowWaterClear) {
            return false;
        }
        
        // Check if bed has a pattern to clear
        SheetPattern currentPattern = accessor.bedsheets$getSheetPattern();
        if (currentPattern == null || !currentPattern.hasPattern()) {
            return false;
        }
        
        // Check if bed is occupied
        if (accessor.bedsheets$isOccupied()) {
            return false;
        }
        
        // Create banner item from current pattern
        ItemStack bannerDrop = BannerUtils.toBanner(currentPattern);
        
        // Clear pattern
        accessor.bedsheets$setSheetPattern(null);
        accessor.bedsheets$setSheetOwner(null);
        bedBlockEntity.markDirty();
        
        // Drop banner
        if (!bannerDrop.isEmpty()) {
            player.dropItem(bannerDrop, false);
        }
        
        // Play sound
        world.playSound(null, headPos, SoundEvents.BLOCK_WOOL_BREAK, SoundCategory.BLOCKS,
                       0.8f, 0.8f + world.random.nextFloat() * 0.4f);
        
        // Sync to clients
        if (player instanceof ServerPlayerEntity serverPlayer) {
            BedSheetsPackets.sendPatternClear(serverPlayer, headPos);
        }
        
        BedSheetsMod.LOGGER.debug("Cleared bed pattern at {} with water bucket", headPos);
        return true;
    }
    
    /**
     * Handles making the bed (resetting messiness).
     */
    private boolean handleBedMaking(World world, BlockPos headPos, PlayerEntity player,
                                  BedBlockEntityAccessor accessor, BedBlockEntity bedBlockEntity) {
        
        // Check if bed is occupied
        if (accessor.bedsheets$isOccupied()) {
            return false;
        }
        
        // Check if bed is already made
        int currentMessiness = accessor.bedsheets$getMessiness();
        if (currentMessiness == 0) {
            return false; // Already made
        }
        
        // Reset messiness
        accessor.bedsheets$setMessiness(0);
        bedBlockEntity.markDirty();
        
        // Play sound
        world.playSound(null, headPos, SoundEvents.BLOCK_WOOL_PLACE, SoundCategory.BLOCKS,
                       0.6f, 1.2f + world.random.nextFloat() * 0.2f);
        
        // TODO: Spawn particles for bed making animation
        
        // Sync to clients
        if (player instanceof ServerPlayerEntity serverPlayer) {
            BedSheetsPackets.sendMessinessUpdate(serverPlayer, headPos, 0);
        }
        
        BedSheetsMod.LOGGER.debug("Made bed at {} (reset messiness from {})", headPos, currentMessiness);
        return true;
    }
}